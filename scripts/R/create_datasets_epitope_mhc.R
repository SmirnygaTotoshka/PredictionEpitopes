library(vroom)
library(dplyr)
library(caret)
library(stringr)
set.seed(9)
iedb.bind = vroom("data/source/epi_mhc_bind_human_total.csv",delim = ";", show_col_types = F) %>% 
  mutate(as_char_value = if_else(as_char_value == 'Negative', 'Negative', 'Positive'))

iedb.elution = vroom("data/source/epi_mhc_elution_human_total.csv",delim = ";",col_types = cols(as_num_value = "d", 
                                                                                                as_inequality = "c",
                                                                                                units = "c",
                                                                                                as_num_subjects = "i",
                                                                                                as_num_responded = "i",
                                                                                                as_response_frequency = "i",
                                                                                                e_ref_start = "i",
                                                                                                e_ref_end = "i"), show_col_types = F)%>% 
  mutate(as_char_value = if_else(as_char_value == 'Negative', 'Negative', 'Positive'))
  
flurry = vroom("data/source/MHCflurry.csv", delim = ",",show_col_types = FALSE)
flurry.bind = flurry %>% filter(measurement_kind == 'affinity') %>% filter(grepl("HLA", allele)) 
flurry.elution = flurry %>% filter(measurement_kind == 'mass_spec') %>% filter(grepl("HLA", allele)) 

class_i_allelic_cutoff = read.csv2("data/class_i_allelic_cutoff.csv",dec = ".") %>% select(-2)

iedb.cols = c("reference_id",
              "as_char_value",
              "source_antigen_accession",
              "epitope",
              "name",
              "organism_name",
              "HLA_allele")

flurry.cols = c("HLA_allele", 
                "epitope",
                "measurement_value",
                "measurement_inequality",
                "measurement_type",
                "measurement_source")

common.cols = c("epitope", 
                "HLA_allele", 
                "activity", 
                "sequence",
                "as_char_value",
                "source_name",
                "source_accession",
                "organism_name",
                "inequality",
                "value",
                "units",
                "affinity_cutoff",
                "reference")
#prepare MHC I datasets

iedb.bind.I = iedb.bind %>% 
  filter(class == "I") %>% 
  filter(grepl("^HLA-[ABC]\\*\\d{2}:\\d{2}$", chain_i_name)) %>% 
  filter(grepl("^[ACDEFGHIKLMNPQRSTVWYU]+$", linear_peptide_seq)) %>% 
  rename("HLA_allele" = "chain_i_name", "epitope" = "linear_peptide_seq") %>% 
  left_join(x = ., y = class_i_allelic_cutoff, by = c("HLA_allele" = "Allele")) %>% 
  mutate(affinity_cutoff = if_else(is.na(affinity_cutoff), 500, affinity_cutoff),
         as_char_value = if_else((as_inequality == "<" | as_inequality == "=") & 
                                   as_num_value <= affinity_cutoff & units == "nM",
                                 "Positive", "Negative", missing = as_char_value),
         activity = as.factor(if_else(as_char_value == "Negative",
                            paste0("!",HLA_allele),
                            HLA_allele)),
         sequence = epitope,
         reference_id = as.character(reference_id)) %>% 
  rename("source_name" = "name",
         "source_accession" = "source_antigen_accession",
         "inequality" = "as_inequality",
         "value" = "as_num_value",
         "reference" = "reference_id") %>% 
  select(all_of(common.cols))

#elution hasnt measurements
iedb.elution.I = iedb.elution %>% 
  filter(class == "I") %>% 
  filter(grepl("^HLA-[ABC]\\*\\d{2}:\\d{2}$", chain_i_name)) %>% 
  filter(grepl("^[ACDEFGHIKLMNPQRSTVWYU]+$", linear_peptide_seq)) %>% 
  rename("HLA_allele" = "chain_i_name", "epitope" = "linear_peptide_seq")%>% 
  left_join(x = ., y = class_i_allelic_cutoff, by = c("HLA_allele" = "Allele")) %>% 
  mutate(affinity_cutoff = if_else(is.na(affinity_cutoff), 500, affinity_cutoff),
         as_char_value = if_else((as_inequality == "<" | as_inequality == "=") & 
                                   as_num_value <= affinity_cutoff & units == "nM",
                                 "Positive", "Negative", missing = as_char_value),
         activity = as.factor(if_else(as_char_value == "Negative",
                            paste0("!",HLA_allele),
                            HLA_allele)),
         sequence = epitope,
         reference_id = as.character(reference_id)) %>% 
  rename("source_name" = "name",
         "source_accession" = "source_antigen_accession",
         "inequality" = "as_inequality",
         "value" = "as_num_value",
         "reference" = "reference_id") %>% 
  select(all_of(common.cols))

flurry.bind.new = flurry.bind %>% 
  rename("HLA_allele" = "allele", "epitope" = "peptide") %>% 
  select(all_of(flurry.cols)) %>% 
  filter(grepl("^HLA-[ABC]\\*\\d{2}:\\d{2}$", HLA_allele)) %>% 
  filter(grepl("^[ACDEFGHIKLMNPQRSTVWYU]+$", epitope)) %>% 
  left_join(x = ., y = class_i_allelic_cutoff, by = c("HLA_allele" = "Allele")) %>% 
  mutate(affinity_cutoff = if_else(is.na(affinity_cutoff), 500, affinity_cutoff),
         as_char_value = if_else((measurement_inequality == "<" | measurement_inequality == "=") & 
                                   measurement_value <= affinity_cutoff,
                                 "Positive", "Negative"),
         activity = as.factor(if_else(as_char_value == "Negative",
                            paste0("!",HLA_allele),
                            HLA_allele)),
         sequence = epitope,
         source_name = NA,
         source_accession = NA,
         organism_name = NA,
         units = "nM") %>% 
  rename("value" = "measurement_value",
         "inequality" = "measurement_inequality",
         "reference" = "measurement_source") %>% 
  select(all_of(common.cols))

flurry.elution.new = flurry.elution %>% 
  rename("HLA_allele" = "allele", "epitope" = "peptide") %>% 
  select(all_of(flurry.cols)) %>% 
  filter(grepl("^HLA-[ABC]\\*\\d{2}:\\d{2}$", HLA_allele)) %>% 
  filter(grepl("^[ACDEFGHIKLMNPQRSTVWYU]+$", epitope)) %>% 
  left_join(x = ., y = class_i_allelic_cutoff, by = c("HLA_allele" = "Allele")) %>% 
  mutate(affinity_cutoff = if_else(is.na(affinity_cutoff), 500, affinity_cutoff),
         as_char_value = if_else((measurement_inequality == "<" | measurement_inequality == "=") & 
                                   measurement_value <= affinity_cutoff,
                                 "Positive", "Negative"),
         activity = as.factor(if_else(as_char_value == "Negative",
                            paste0("!",HLA_allele),
                            HLA_allele)),
         sequence = epitope,
         source_name = NA,
         source_accession = NA,
         organism_name = NA,
         units = "nM") %>% 
  rename("value" = "measurement_value",
         "inequality" = "measurement_inequality",
         "reference" = "measurement_source") %>% 
  select(all_of(common.cols))

combined.total.I = bind_rows(iedb.bind.I, iedb.elution.I, flurry.bind.new, flurry.elution.new, .id = "database_and_assay")
contra = combined.total.I %>%
  mutate(pair1 = paste(epitope, HLA_allele, sep = "_")) %>% 
  group_by(pair1) %>% 
  summarise(num_types_outcomes = length(unique(as_char_value))) %>% 
  filter(num_types_outcomes != 1) %>% 
  pull(pair1)

combined.total.I.clean = combined.total.I %>% 
  mutate(pair = paste(epitope, activity, sep = "_"),
         pair1 = paste(epitope, HLA_allele, sep = "_")) %>% 
  filter(!duplicated(pair)) %>% 
  filter(!pair1 %in% contra) %>% 
  select(-c("pair", "pair1"))

total.I.idx = createFolds(combined.total.I.clean$activity, k = 5, returnTrain = T)
combined.total.I.clean[total.I.idx$Fold1,"Fold1"] = T
combined.total.I.clean[-total.I.idx$Fold1,"Fold1"] = F
combined.total.I.clean[total.I.idx$Fold2,"Fold2"] = T
combined.total.I.clean[-total.I.idx$Fold2,"Fold2"] = F
combined.total.I.clean[total.I.idx$Fold3,"Fold3"] = T
combined.total.I.clean[-total.I.idx$Fold3,"Fold3"] = F
combined.total.I.clean[total.I.idx$Fold4,"Fold4"] = T
combined.total.I.clean[-total.I.idx$Fold4,"Fold4"] = F
combined.total.I.clean[total.I.idx$Fold5,"Fold5"] = T
combined.total.I.clean[-total.I.idx$Fold5,"Fold5"] = F

combined.positive.I.clean = combined.total.I.clean %>% filter(as_char_value == "Positive")
positive.I.idx = createFolds(combined.positive.I.clean$activity, k = 5, returnTrain = T)
combined.positive.I.clean[positive.I.idx$Fold1,"Fold1"] = T
combined.positive.I.clean[-positive.I.idx$Fold1,"Fold1"] = F
combined.positive.I.clean[positive.I.idx$Fold2,"Fold2"] = T
combined.positive.I.clean[-positive.I.idx$Fold2,"Fold2"] = F
combined.positive.I.clean[positive.I.idx$Fold3,"Fold3"] = T
combined.positive.I.clean[-positive.I.idx$Fold3,"Fold3"] = F
combined.positive.I.clean[positive.I.idx$Fold4,"Fold4"] = T
combined.positive.I.clean[-positive.I.idx$Fold4,"Fold4"] = F
combined.positive.I.clean[positive.I.idx$Fold5,"Fold5"] = T
combined.positive.I.clean[-positive.I.idx$Fold5,"Fold5"] = F

folds = c("Fold1", "Fold2", "Fold3", "Fold4", "Fold5")

vroom_write(combined.total.I.clean,"data/AA_for_transplantology/with_folds/combined_total_I_clean_with_folds.csv", delim = ";")
vroom_write(combined.total.I.clean %>% select(-all_of(folds)),"data/AA_for_transplantology/combined_total_I_clean.csv", delim = ";")

vroom_write(combined.positive.I.clean,"data/AA_for_transplantology/with_folds/combined_positive_I_clean_with_folds.csv", delim = ";")
vroom_write(combined.positive.I.clean %>% select(-all_of(folds)),"data/AA_for_transplantology/combined_positive_I_clean.csv", delim = ";")

for (f in folds) {
  vroom_write(combined.total.I.clean %>% filter(get(f)) %>% select(-all_of(folds)),paste0("data/AA_for_transplantology/combined_total_I_clean_",f,".csv"), delim = ";")
  vroom_write(combined.positive.I.clean %>% filter(get(f)) %>% select(-all_of(folds)),paste0("data/AA_for_transplantology/combined_positive_I_clean_",f,".csv"), delim = ";")
}

#prepare MHC II datasets
common.cols.II = c("epitope", 
                "HLA_allele.A", 
                "HLA_allele.B",
                "activity", 
                "activity.A",
                "activity.B",
                "sequence",
                "as_char_value",
                "source_name",
                "source_accession",
                "organism_name",
                "inequality",
                "value",
                "units",
                "affinity_cutoff",
                "reference")
iedb.bind.II = iedb.bind %>% 
  filter(class == "II") %>% 
  mutate(chain_ii_name = paste0("HLA-",str_split(displayed_restriction,"/")[[1]][2])) %>% 
  filter(grepl("^HLA-D[RPQ]A\\d{0,1}\\*\\d{2}:\\d{2}$", chain_i_name)) %>% 
  filter(grepl("^HLA-D[RPQ]B\\d{0,1}\\*\\d{2}:\\d{2}$", chain_ii_name)) %>%
  filter(grepl("^[ACDEFGHIKLMNPQRSTVWYU]+$", linear_peptide_seq)) %>% 
  mutate(affinity_cutoff = 1000,
         as_char_value = if_else((as_inequality == "<" | as_inequality == "=") & 
                                   as_num_value <= affinity_cutoff & units == "nM",
                                 "Positive", "Negative", missing = as_char_value),
         activity = as.factor(if_else(as_char_value == "Negative",
                                      paste0("!",displayed_restriction),
                                      displayed_restriction)),
         activity.A = as.factor(if_else(as_char_value == "Negative",
                                      paste0("!",chain_i_name),
                                      chain_i_name)),
         activity.B = as.factor(if_else(as_char_value == "Negative",
                                      paste0("!",chain_ii_name),
                                      chain_ii_name)),
         sequence = linear_peptide_seq,
         reference_id = as.character(reference_id)) %>% 
  rename("HLA_allele.A" = "chain_i_name", 
         "HLA_allele.B" = "chain_ii_name", 
         "epitope" = "linear_peptide_seq",
         "source_name" = "name",
         "source_accession" = "source_antigen_accession",
         "inequality" = "as_inequality",
         "value" = "as_num_value",
         "reference" = "reference_id") %>% 
  select(all_of(common.cols.II))

#elution hasnt measurements
iedb.elution.II = iedb.elution %>% 
  filter(class == "II") %>% 
  mutate(chain_ii_name = paste0("HLA-",str_split(displayed_restriction,"/")[[1]][2])) %>% 
  filter(grepl("^HLA-D[RPQ]A\\d{0,1}\\*\\d{2}:\\d{2}$", chain_i_name)) %>% 
  filter(grepl("^HLA-D[RPQ]B\\d{0,1}\\*\\d{2}:\\d{2}$", chain_ii_name)) %>%
  filter(grepl("^[ACDEFGHIKLMNPQRSTVWYU]+$", linear_peptide_seq)) %>% 
  mutate(affinity_cutoff = 1000,
         as_char_value = if_else((as_inequality == "<" | as_inequality == "=") & 
                                   as_num_value <= affinity_cutoff & units == "nM",
                                 "Positive", "Negative", missing = as_char_value),
         activity = as.factor(if_else(as_char_value == "Negative",
                                      paste0("!",displayed_restriction),
                                      displayed_restriction)),
         activity.A = as.factor(if_else(as_char_value == "Negative",
                                        paste0("!",chain_i_name),
                                        chain_i_name)),
         activity.B = as.factor(if_else(as_char_value == "Negative",
                                        paste0("!",chain_ii_name),
                                        chain_ii_name)),
         sequence = linear_peptide_seq,
         reference_id = as.character(reference_id)) %>% 
  rename("HLA_allele.A" = "chain_i_name", 
         "HLA_allele.B" = "chain_ii_name", 
         "epitope" = "linear_peptide_seq",
         "source_name" = "name",
         "source_accession" = "source_antigen_accession",
         "inequality" = "as_inequality",
         "value" = "as_num_value",
         "reference" = "reference_id") %>% 
  select(all_of(common.cols.II))

combined.total.II = bind_rows(iedb.bind.II, iedb.elution.II, .id = "assay")
contra.II = combined.total.II %>%
  mutate(pair1 = paste(epitope, HLA_allele.A, HLA_allele.B, sep = "_")) %>% 
  group_by(pair1) %>% 
  summarise(num_types_outcomes = length(unique(as_char_value))) %>% 
  filter(num_types_outcomes != 1) %>% 
  pull(pair1)

combined.total.II.clean = combined.total.II %>% 
  mutate(pair = paste(epitope, activity.A, activity.B, sep = "_"),
         pair1 = paste(epitope, HLA_allele.A, HLA_allele.B, sep = "_")) %>% 
  filter(!duplicated(pair)) %>% 
  filter(!pair1 %in% contra) %>% 
  select(-c("pair", "pair1"))

vroom_write(combined.total.II.clean,"data/AA_for_transplantology/combined_total_II_clean.csv", delim = ";")
combined.positive.II.clean = combined.total.II.clean %>% filter(as_char_value == "Positive")
vroom_write(combined.positive.II.clean,"data/AA_for_transplantology/combined_positive_II_clean.csv", delim = ";")
# total.II.idx.A = createFolds(combined.total.II.clean$activity, k = 5, returnTrain = T)
# combined.total.II.clean[total.II.idx$Fold1,"Fold1"] = T
# combined.total.II.clean[-total.II.idx$Fold1,"Fold1"] = F
# combined.total.II.clean[total.II.idx$Fold2,"Fold2"] = T
# combined.total.II.clean[-total.II.idx$Fold2,"Fold2"] = F
# combined.total.II.clean[total.II.idx$Fold3,"Fold3"] = T
# combined.total.II.clean[-total.II.idx$Fold3,"Fold3"] = F
# combined.total.II.clean[total.II.idx$Fold4,"Fold4"] = T
# combined.total.II.clean[-total.II.idx$Fold4,"Fold4"] = F
# combined.total.II.clean[total.II.idx$Fold5,"Fold5"] = T
# combined.total.II.clean[-total.II.idx$Fold5,"Fold5"] = F
# 
# combined.positive.II.clean = combined.total.II.clean %>% filter(as_char_value == "Positive")
# positive.II.idx = createFolds(combined.positive.II.clean$activity, k = 5, returnTrain = T)
# combined.positive.II.clean[positive.II.idx$Fold1,"Fold1"] = T
# combined.positive.II.clean[-positive.II.idx$Fold1,"Fold1"] = F
# combined.positive.II.clean[positive.II.idx$Fold2,"Fold2"] = T
# combined.positive.II.clean[-positive.II.idx$Fold2,"Fold2"] = F
# combined.positive.II.clean[positive.II.idx$Fold3,"Fold3"] = T
# combined.positive.II.clean[-positive.II.idx$Fold3,"Fold3"] = F
# combined.positive.II.clean[positive.II.idx$Fold4,"Fold4"] = T
# combined.positive.II.clean[-positive.II.idx$Fold4,"Fold4"] = F
# combined.positive.II.clean[positive.II.idx$Fold5,"Fold5"] = T
# combined.positive.II.clean[-positive.II.idx$Fold5,"Fold5"] = F
# 
# folds = c("Fold1", "Fold2", "Fold3", "Fold4", "Fold5")

# vroom_write(combined.total.II.clean,"data/AA_for_transplantology/with_folds/combined_total_II_clean_with_folds.csv", delim = ";")
# vroom_write(combined.total.II.clean %>% select(-all_of(folds)),"data/AA_for_transplantology/combined_total_II_clean.csv", delim = ";")
# 
# vroom_write(combined.positive.II.clean,"data/AA_for_transplantology/with_folds/combined_positive_II_clean_with_folds.csv", delim = ";")
# vroom_write(combined.positive.II.clean %>% select(-all_of(folds)),"data/AA_for_transplantology/combined_positive_II_clean.csv", delim = ";")
# 
# for (f in folds) {
#   vroom_write(combined.total.II.clean %>% filter(get(f)) %>% select(-all_of(folds)),paste0("data/AA_for_transplantology/combined_total_II_clean_",f,".csv"), delim = ";")
#   vroom_write(combined.positive.II.clean %>% filter(get(f)) %>% select(-all_of(folds)),paste0("data/AA_for_transplantology/combined_positive_II_clean_",f,".csv"), delim = ";")
# }
