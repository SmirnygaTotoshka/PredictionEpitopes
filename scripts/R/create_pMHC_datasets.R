library(dplyr)
library(stringr)
library(vroom)

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
           activity)

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
           activity)

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
