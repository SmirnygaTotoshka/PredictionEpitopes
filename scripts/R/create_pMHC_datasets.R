library(dplyr)
library(stringr)
library(vroom)
library(caret)
#IEDB data
bind.dataset = vroom("data/source/iedb_bind_assays_clean.tsv",delim = "\t",show_col_types = FALSE)

cols = c("as_char_value",
         "assay_type",
         "source_antigen_accession",
         "linear_peptide_seq",
         "name",
         "organism_name",
         "chain_i_name")

positive.iedb = bind.dataset %>% 
  select(all_of(cols)) %>% 
  filter(as_char_value == 'Positive') %>% 
  mutate(activity = chain_i_name) %>% 
  select(-c("chain_i_name"))

negative.iedb = bind.dataset %>% 
  select(all_of(cols)) %>% 
  filter(as_char_value == 'Negative') %>% 
  mutate(activity = paste0("!",chain_i_name)) %>% #!<allele> for negative models
  select(-c("chain_i_name"))

#MHCflurry 2.0 data
flurry.cols = c("allele", 
                "peptide",
                "measurement_value",
                "measurement_inequality",
                "measurement_type",
                "measurement_source")

flurry.data = vroom("data/source/MHCflurry.csv", delim = ",",show_col_types = FALSE) %>% 
  filter(measurement_kind == 'affinity') %>% 
  filter(grepl("HLA", allele)) %>% 
  select(all_of(flurry.cols)) %>% 
  mutate("as_char_value" = if_else(measurement_value <= 5000 & measurement_inequality != ">",
                                   "Positive",
                                   "Negative"))
table(flurry.data$as_char_value)

positive.flurry = flurry.data %>% 
  filter(as_char_value == 'Positive') %>% 
  mutate(activity = allele) %>% 
  select(-c("measurement_type","measurement_value","allele", "measurement_inequality")) %>% 
  mutate(source_antigen_accession = NA,
         source_name = NA,
         source_organism_name = NA) %>% 
  relocate(as_char_value, 
           measurement_source,
           source_antigen_accession,
           peptide,
           source_name,
           source_organism_name,
           activity) %>% 
  filter(grepl("^HLA-[ABC]\\*\\d{2}:\\d{2}$", activity)) %>% 
  filter(grepl("[ACDEFGHIKLMNPQRSTVWY]",peptide))

negative.flurry = flurry.data %>% 
  filter(as_char_value == 'Negative') %>% 
  mutate(activity = paste0("!",allele)) %>% 
  select(-c("measurement_type","measurement_value","allele", "measurement_inequality"))%>% 
  mutate(source_antigen_accession = NA,
         source_name = NA,
         source_organism_name = NA) %>% 
  relocate(as_char_value, 
           measurement_source,
           source_antigen_accession,
           peptide,
           source_name,
           source_organism_name,
           activity)%>% 
  filter(grepl("^!HLA-[ABC]\\*\\d{2}:\\d{2}$", activity)) %>% 
  filter(grepl("[ACDEFGHIKLMNPQRSTVWY]",peptide))

final.cols = c("as_char_value",
               "assay_type",
               "source_antigen_accession",
               "epitope",
               "source_name",
               "source_organism_name",
               "activity")

colnames(positive.iedb) = final.cols
colnames(negative.iedb) = final.cols
colnames(positive.flurry) = final.cols
colnames(negative.flurry) = final.cols

positive = bind_rows(positive.iedb, positive.flurry, .id = "database")
negative = bind_rows(negative.iedb, negative.flurry, .id = "database")

positive.stat = positive %>% group_by(activity) %>%
  summarise(n_epi = length(epitope),
            unique_epi = length(unique(epitope)),
            unique_species = length(unique(source_organism_name))) %>%
  arrange(.by_group = T) %>%
  filter(unique_epi >= 15) # at least 3 epitope for 5-fold CV

negative.stat = negative %>% group_by(activity) %>%
  summarise(n_epi = length(epitope),
            unique_epi = length(unique(epitope)),
            unique_species = length(unique(source_organism_name))) %>%
  arrange(.by_group = T) %>%
  filter(unique_epi >= 15) # at least 3 epitope for 5-fold CV

filtered.positive = positive %>% filter(activity %in% positive.stat$activity)
filtered.negative = negative %>% filter(activity %in% negative.stat$activity)

set.seed(9)#my basketball player number
filtered.positive$activity = as.factor(filtered.positive$activity)
filtered.negative$activity = as.factor(filtered.negative$activity)

pos.train.idx = createFolds(filtered.positive$activity, k = 5, returnTrain = T)
neg.train.idx = createFolds(filtered.negative$activity, k = 5, returnTrain = T)

#check all splits has all alleles
#positive
for (i in 1:5) {
  tr.fold = filtered.positive[pos.train.idx[[i]],]
  print(paste("train",i,length(unique(tr.fold$activity)),length(unique(filtered.positive$activity))))
  test.fold = filtered.positive[-pos.train.idx[[i]],]
  print(paste("test",i,length(unique(test.fold$activity)),length(unique(filtered.positive$activity))))
}
#negative
for (i in 1:5) {
  tr.fold = filtered.negative[neg.train.idx[[i]],]
  test.fold = filtered.negative[-neg.train.idx[[i]],]
  print(paste("train",i,length(unique(tr.fold$activity)),length(unique(filtered.negative$activity))))
  print(paste("test",i,length(unique(test.fold$activity)),length(unique(filtered.negative$activity))))
}

#save combined datasets
for (i in 1:5) {
  pos.train = filtered.positive[pos.train.idx[[i]],]
  pos.test = filtered.positive[-pos.train.idx[[i]],]
  neg.train = filtered.negative[neg.train.idx[[i]],]
  neg.test = filtered.negative[-neg.train.idx[[i]],]
  
  vroom_write(pos.train, paste0("data/bind_train_dataset/combined/positive_train_",i,".csv"),delim = ";")
  vroom_write(pos.test, paste0("data/bind_train_dataset/combined/positive_test_",i,".csv"),delim = ";")
  vroom_write(neg.train, paste0("data/bind_train_dataset/combined/negative_train_",i,".csv"),delim = ";")
  vroom_write(neg.test, paste0("data/bind_train_dataset/combined/negative_test_",i,".csv"),delim = ";")
  
}

combined = bind_rows(filtered.positive, filtered.negative) %>% 
  mutate(allele = if_else(as_char_value == "Positive", activity, str_sub(activity,2)))

combined$allele = as.factor(combined$allele)

failed.alleles = c()
for (a in levels(combined$allele)) {
  allele.name.for.file = str_remove_all(a,"[*:]")
  PATH = paste0("data/bind_train_dataset/by_alleles/",allele.name.for.file)
  print(a)
  allele.data = subset(combined, allele == a)
  if(length(unique(allele.data$activity)) == 2){
    if (!dir.exists(PATH)){
      dir.create(PATH)
    }
    idx = createFolds(allele.data$activity, k = 5, returnTrain = T)
    for (i in 1:5) {
      train = allele.data[idx[[i]],]
      test = allele.data[-idx[[i]],]
      
      vroom_write(train, paste0(PATH,"/",allele.name.for.file,"_train_",i,".csv"),delim = ";")
      vroom_write(test, paste0(PATH,"/",allele.name.for.file,"_test_",i,".csv"),delim = ";")
    } 
    print("Created")
  }
  else{
      print("There aren`t alternative activity")
      failed.alleles = c(failed.alleles, a)
  }
}
