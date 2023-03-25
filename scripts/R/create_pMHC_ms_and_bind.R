library(vroom)
library(dplyr)

iedb.bind = vroom("data/source/iedb_bind_assays_clean.tsv", delim = "\t", show_col_types = F)
iedb.elution = vroom("data/source/iedb_elution_assays_clean.tsv", delim = "\t", show_col_types = F)
flurry = vroom("data/source/MHCflurry.csv", delim = ",", show_col_types = F)

flurry.cols = c("allele", 
                "peptide",
                "measurement_value",
                "measurement_inequality",
                "measurement_type",
                "measurement_source")

iedb.cols = c("as_char_value",
              "assay_type",
              "as_num_value",
              "as_inequality",
              "reference_id",
              "linear_peptide_seq",
              "chain_i_name")

flurry.bind = flurry %>% 
  filter(measurement_kind == "affinity") %>% 
  filter(grepl("^HLA-[ABC]\\*\\d{2}:\\d{2}$", allele)) %>% 
  filter(grepl("^[ACDEFGHIKLMNPQRSTVWYU]+$", peptide)) %>% 
  select(all_of(flurry.cols)) %>% #TODO maybe change to 5000
  mutate("as_char_value" = if_else(measurement_value <= 100 & measurement_inequality != ">",
                                   "Positive",
                                   "Negative"),
         "activity" = if_else(as_char_value == "Positive",
                              allele,
                              paste0("!",allele))) %>%  #threshold from https://doi.org/10.1016/j.cels.2020.06.010
  rename("epitope" = "peptide") %>% 
  relocate(as_char_value,
           epitope,
           activity,
           allele,
           measurement_type,
           measurement_inequality,
           measurement_value,
           measurement_source)

flurry.elution = flurry %>% 
    filter(measurement_kind == "mass_spec") %>% 
    filter(grepl("^HLA-[ABC]\\*\\d{2}:\\d{2}$", allele)) %>% 
    filter(grepl("^[ACDEFGHIKLMNPQRSTVWYU]+$", peptide)) %>% 
    select(all_of(flurry.cols)) %>% 
    mutate("as_char_value" = if_else(measurement_value <= 100 & measurement_inequality != ">",
                                     "Positive",
                                     "Negative"),
           "activity" = if_else(as_char_value == "Positive",
                                allele,
                                paste0("!",allele))) %>% 
  rename("epitope" = "peptide") %>% 
  relocate(as_char_value,
           epitope,
           activity,
           allele,
           measurement_type,
           measurement_inequality,
           measurement_value,
           measurement_source)

iedb.bind = iedb.bind %>% 
  filter(grepl("^HLA-[ABC]\\*\\d{2}:\\d{2}$", chain_i_name)) %>% 
  filter(grepl("^[ACDEFGHIKLMNPQRSTVWYU]+$", linear_peptide_seq)) %>% 
  select(all_of(iedb.cols)) %>% 
  mutate("activity" = if_else(as_char_value == "Positive",
                              chain_i_name,
                              paste0("!",chain_i_name))) %>% 
  rename("epitope" = "linear_peptide_seq",
         "allele" = "chain_i_name",
         "measurement_type" = "assay_type",
         "measurement_inequality" = "as_inequality",
         "measurement_value" = "as_num_value",
         "measurement_source" = "reference_id") %>% 
  relocate(as_char_value,
           epitope,
           activity,
           allele,
           measurement_type,
           measurement_inequality,
           measurement_value,
           measurement_source)

iedb.elution = iedb.elution %>% 
  filter(grepl("^HLA-[ABC]\\*\\d{2}:\\d{2}$", chain_i_name)) %>% 
  filter(grepl("^[ACDEFGHIKLMNPQRSTVWYU]+$", linear_peptide_seq)) %>% 
  select(all_of(iedb.cols)) %>% 
  mutate("activity" = if_else(as_char_value == "Positive",
                              chain_i_name,
                              paste0("!",chain_i_name))) %>% 
  rename("epitope" = "linear_peptide_seq",
         "allele" = "chain_i_name",
         "measurement_type" = "assay_type",
         "measurement_inequality" = "as_inequality",
         "measurement_value" = "as_num_value",
         "measurement_source" = "reference_id") %>%
  relocate(as_char_value,
           epitope,
           activity,
           allele,
           measurement_type,
           measurement_inequality,
           measurement_value,
           measurement_source)

iedb.bind$measurement_source = as.character(iedb.bind$measurement_source)
iedb.elution$measurement_source = as.character(iedb.elution$measurement_source)

bind.contra = bind_rows(iedb.bind, flurry.bind) %>%
  mutate(pair1 = paste(epitope, allele, sep = "_")) %>% 
  group_by(pair1) %>% 
  summarise(num_types_outcomes = length(unique(as_char_value))) %>% 
  filter(num_types_outcomes != 1) %>% 
  pull(pair1)

bind.dataset = bind_rows(iedb.bind, flurry.bind) %>% 
  mutate(pair = paste(epitope, activity, sep = "_"),
         pair1 = paste(epitope, allele, sep = "_")) %>% 
  filter(!duplicated(pair)) %>% 
  filter(!pair1 %in% bind.contra) %>% 
  select(-c("pair", "pair1"))

elution.contra = bind_rows(iedb.elution, flurry.elution) %>%
  mutate(pair1 = paste(epitope, allele, sep = "_")) %>% 
  group_by(pair1) %>% 
  summarise(num_types_outcomes = length(unique(as_char_value))) %>% 
  filter(num_types_outcomes != 1) %>% 
  pull(pair1)

elution.dataset = bind_rows(iedb.elution, flurry.elution) %>% 
  mutate(pair = paste(epitope, activity, sep = "_"),
         pair1 = paste(epitope, allele, sep = "_")) %>% 
  filter(!duplicated(pair)) %>% 
  filter(!pair1 %in% elution.contra) %>% 
  select(-c("pair", "pair1"))

combined.contra = bind_rows(bind.dataset, elution.dataset) %>%
  mutate(pair1 = paste(epitope, allele, sep = "_")) %>% 
  group_by(pair1) %>% 
  summarise(num_types_outcomes = length(unique(as_char_value))) %>% 
  filter(num_types_outcomes != 1) %>% 
  pull(pair1)


combined = bind_rows(bind.dataset, elution.dataset)%>% 
  mutate(pair = paste(epitope, activity, sep = "_"),
         pair1 = paste(epitope, allele, sep = "_")) %>% 
  filter(!duplicated(pair))%>% 
  filter(!pair1 %in% elution.contra) %>% 
  select(-c("pair", "pair1"))

vroom_write(bind.dataset, "data/check_assays_pMHC/bind.csv", delim = ";")
vroom_write(elution.dataset, "data/check_assays_pMHC/elution.csv", delim = ";")
vroom_write(combined, "data/check_assays_pMHC/combined.csv", delim = ";")

