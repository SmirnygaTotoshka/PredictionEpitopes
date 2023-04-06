library(dplyr)
library(readxl)
set.seed(9)
total.sample = read.csv2("data/AA_for_transplantology/combined_total_I_clean.csv")
negative.sample = total.sample %>% 
  filter(grepl("\\!", activity)) 

read.csv2("data/AA_for_transplantology/combined_positive_I_clean_test_Fold1.csv") %>% 
  bind_rows(negative.sample) %>% 
  write.csv2(file = "data/cross_val/cross_val_negative/negative_fold1.csv")

read.csv2("data/AA_for_transplantology/combined_positive_I_clean_test_Fold2.csv") %>% 
  bind_rows(negative.sample) %>% 
  write.csv2(file = "data/cross_val/cross_val_negative/negative_fold2.csv")

read.csv2("data/AA_for_transplantology/combined_positive_I_clean_test_Fold3.csv") %>% 
  bind_rows(negative.sample) %>% 
  write.csv2(file = "data/cross_val/cross_val_negative/negative_fold3.csv")

read.csv2("data/AA_for_transplantology/combined_positive_I_clean_test_Fold4.csv") %>% 
  bind_rows(negative.sample) %>% 
  write.csv2(file = "data/cross_val/cross_val_negative/negative_fold4.csv")

read.csv2("data/AA_for_transplantology/combined_positive_I_clean_test_Fold5.csv") %>% 
  bind_rows(negative.sample) %>% 
  write.csv2(file = "data/cross_val/cross_val_negative/negative_fold5.csv")
