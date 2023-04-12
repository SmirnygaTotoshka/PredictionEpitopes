#Two data sources
#1) MHC BN - 10.1186/1756-0500-2-61 parsed by python script
#2) TAPREG - 10.1002/prot.22535 - supplementary
library(dplyr)
library(vroom)
library(stringr)
library(readxl)
library(caret)
library(ggplot2)
#MHC BN
mhcbn = vroom("data/source/TAP/MHCBN_2023-02-16.tsv", delim = "\t") %>% 
  select(-1) %>% 
  filter(`MHC Allele` == "TAP") %>% 
  filter(`Host Organism` == "HUMAN") %>% 
  filter(!is.na(Comment)) %>% 
  mutate(dup_check = paste(`Peptide Sequence`, Comment)) %>% 
  filter(!duplicated(dup_check)) %>% 
  filter(grepl("Relative", Comment)) %>% 
  filter(!grepl("approx", Comment)) %>% 
  filter(grepl("nM|uM",Comment,ignore.case = T)) %>% 
  mutate(unit = str_extract(Comment,"\\(([^)]+)\\)",group = T), 
         value = if_else(grepl("u",unit), 
                         sapply(str_split(Comment,"="),function(x){as.double(x[2])}) * 1000,
                         sapply(str_split(Comment,"="),function(x){as.double(x[2])}))) %>% 
  filter(!is.na(value)) %>% 
  mutate(log_IC50_rel = log10(value)) %>% 
  select(all_of(c("Peptide Sequence", "log_IC50_rel"))) %>% 
  rename("PEPTIDE" = "Peptide Sequence")
#activity = if_else(pIC50 < (9 - log10(800)), 0, 1)
table(mhcbn$activity)
#TAPREG supplementary
# 1 - 10.4049/jimmunol.161.2.617 + 10.1186/1745-7580-1-4
# 2 - 5 613-peptide dataset using the purge utility of the Gibbs Sampler (10.1002/pro.5560040820) 
# with an exhaustive method and maximum blosum 62 relatedness scores of 25, 30, 35, and 37.
# 6 - 723 unique 9-mer CD8 T cell epitopes obtained from the IMMUNEEPITOPE and EPIMHC databases
# 7- tapreg parameters

#let tapreg log(IC50_relative) as is pIC50_relative in mhc bn 
first = read_excel("data/source/TAP/TAPREG/prot_22535_sm_supptable1.xls") %>% 
  rename("log_IC50_rel" = "log(IC50_relative)")

second = read_excel("data/source/TAP/TAPREG/prot_22535_sm_supptable2.xls") %>% 
  rename("log_IC50_rel" = "log(IC50_relative)")

third = read_excel("data/source/TAP/TAPREG/prot_22535_sm_supptable3.xls") %>% 
  rename("log_IC50_rel" = "log(IC50_relative)")

fourth = read_excel("data/source/TAP/TAPREG/prot_22535_sm_supptable4.xls") %>% 
  rename("log_IC50_rel" = "log(IC50_relative)")

fifth = read_excel("data/source/TAP/TAPREG/prot_22535_sm_supptable5.xls") %>% 
  rename("log_IC50_rel" = "log(IC50_relative)")

total = bind_rows(mhcbn, first,second,third,fourth,fifth) %>% 
  group_by(PEPTIDE) %>% 
  summarise(median_log10_IC50_rel = median(log_IC50_rel)) %>% 
  mutate(activity = if_else(median_log10_IC50_rel <= (log10(800)), 0, 1))

table(total$activity)

folds = createFolds(total$activity, k = 5, returnTrain = T)

k = 1
for (i in folds) {
  write.csv2(total[i,], paste0("data/TAP/TAP_train_",k,".csv"),row.names = F)
  write.csv2(total[-i,], paste0("data/TAP/TAP_test_",k,".csv"),row.names = F)
  k = k + 1
}
result = read_excel("data/TAP/results.xlsx")
ggplot(result) + 
  geom_point(aes(x = descriptor_level, y = twentyCV, color = activity))+ 
  geom_line(aes(x = descriptor_level, y = twentyCV, color = activity))+
  scale_x_discrete(limits = result$descriptor_level)+
  labs(x = "MNA level",
       y = "AUC20CV", 
       title = "TAP") + 
  guides(color=guide_legend(title="Model type"))
