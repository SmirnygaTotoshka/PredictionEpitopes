#Используемые библиотеки
library(RMySQL)
library(dplyr)
library(stringr)
library(knitr)
library(vroom)

#Их версии
print(paste("RMySQL",packageVersion("RMySQL")))
print(paste("dplyr",packageVersion("dplyr")))
print(paste("stringr",packageVersion("stringr")))
print(paste("knitr",packageVersion("knitr")))
print(paste("vroom",packageVersion("vroom")))

setwd("/home/stotoshka/Documents/Epitops/PredictionEpitopes")

# Подклюение БД и выгрузка данных
# con = RMySQL::dbConnect(RMySQL::MySQL(),
#                         dbname='iedb',
#                         host='localhost',
#                         port=3306,
#                         user='root',
#                         password='meow_root')

# elution.assays = RMySQL::dbReadTable(con, "epi_mhc_bind")
# elution.assays = RMySQL::dbReadTable(con, "epi_mhc_elution")
# vroom_write(elution.assays,"data/source/iedb_bind_assays.tsv")
# vroom_write(elution.assays, "data/source/iedb_elution_assays.tsv")

#Bind assays

elution.assays = vroom("data/source/iedb_elution_assays.tsv",delim = "\t") %>% as.data.frame()
str(elution.assays)

# Критерии фильтрации:
# 1. У каждой записи должен быть источник
# 2. У каждой записи должен быть исход эксперимента
# 3. Последовательности должны быть записаны однобуквенным кодом для аминокислот
# 4. Эпитоп должен находиться в референсной последовательности
# 5. Эпитоп должен быть полным. Это не должен быть участок антигенного сайта или частью большего эпитопа.
# 6. Информация в комментариях не должна противоречить исходу эксперимента 
# 7. Результаты должны быть получены экспериментально, не быть предсказанными
# 8. Эпитопы должны быть иммуногенными
# 9. Не должно быть дефицита по TAP или ERAP1
# 10. Все HLA аллели должны быть записаны в формате, позволяющем однозначно идентифицировать молекулу

#1
sum(is.na(elution.assays$reference_id)) == 0
#2
sum(is.na(elution.assays$as_char_value)) == 0
#3
sum(grepl("^[ACDEFGHIKLMNPQRSTVWYU]+$",elution.assays$linear_peptide_seq)) == nrow(elution.assays)
sum(grepl("^[ACDEFGHIKLMNPQRSTVWYU]+$",elution.assays$linear_peptide_seq)) == nrow(elution.assays)
#4
ind = seq_len(nrow(elution.assays))
has.epitope = sapply(ind,function(i){
  grepl(elution.assays$linear_peptide_seq[i],elution.assays$sequence[i],fixed = T)
})
sum(has.epitope) == nrow(elution.assays)
#5
kable(round(table(elution.assays$e_region_domain_flag) / nrow(elution.assays) * 100,2),col.names = c("Value","Procent"))
#6
print(paste("Количество",length(unique(elution.assays$as_comments))))
print(paste("Количество записей",sum(!is.na(elution.assays$as_comments))))
write.table(unique(elution.assays$as_comments),"data/elution_assays_comments.txt")
kable(table(subset(elution.assays,grepl("did not",elution.assays$as_comments,fixed = T), select = "as_char_value")),caption = "did not")
kable(table(subset(elution.assays,grepl("bad",elution.assays$as_comments,fixed = T), select = "as_char_value")),caption = "bad")
#7
kable(table(subset(elution.assays,grepl("predic",elution.assays$as_comments,ignore.case = T), select = "as_char_value")),caption = "was predicted")
#8
kable(table(subset(elution.assays,grepl("non-immunogenic",elution.assays$as_comments,fixed = T), select = "as_char_value")),caption = "non-immunogenic")
#9
kable(table(subset(elution.assays,grepl("TAP deficient",elution.assays$as_comments,fixed = T), select = "as_char_value")),caption = "TAP deficient")
kable(table(subset(elution.assays,grepl("ERAP1 silencing",elution.assays$as_comments,fixed = T), select = "as_char_value")),caption = "ERAP1 silencing")

#10
sum(grepl("^HLA-[ABC]\\*\\d{2}:\\d{2}$",elution.assays$chain_i_name)) == nrow(elution.assays)

# Условия  4,5, 7-10 не соблюдаются. Исправим. Удалим все несовпадения, исправим противоречия, 
# исправим as_char_value
# Выделим результаты, полученные на TAP-дефицитном и ERAP1-дефицитном материале в отдельный датасет
print(paste("Количество строк до",dim(elution.assays)[1]))

elution.assays[grep("Positive", elution.assays$as_char_value),"as_char_value"] = "Positive"
elution.assays = elution.assays[has.epitope,]
elution.assays = elution.assays[grepl("Exact Epitope", elution.assays$e_region_domain_flag,fixed = T),]

tap.def = elution.assays[grepl("TAP deficient",elution.assays$as_comments,fixed = T),]
erap1.silencing = elution.assays[grepl("ERAP1 silencing",elution.assays$as_comments,fixed = T),]
vroom_write(tap.def,"data/tap_def_elution.csv")
vroom_write(erap1.silencing,"data/erap_silence_elution.csv")

elution.assays = elution.assays[!grepl("TAP deficient",elution.assays$as_comments,fixed = T),]
elution.assays = elution.assays[!grepl("predict",elution.assays$as_comments,ignore.case = T),]
elution.assays = elution.assays[!grepl("ERAP1 silencing",elution.assays$as_comments,fixed = T),]
elution.assays = elution.assays[grepl("^HLA-[ABC]\\*\\d{2}:\\d{2}$",elution.assays$chain_i_name),]

print(paste("Количество строк после",dim(elution.assays)[1]))

#Сохраним и будем дальше с ним работать
vroom_write(elution.assays, "data/source/iedb_elution_assays_clean.tsv")

#----------------------------------------------------------------