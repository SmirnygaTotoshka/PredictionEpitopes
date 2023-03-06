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
# 
# bind.assays = RMySQL::dbReadTable(con, "epi_mhc_bind")
# elution.assays = RMySQL::dbReadTable(con, "epi_mhc_elution")
# vroom_write(bind.assays,"data/source/iedb_bind_assays.tsv")
# vroom_write(elution.assays, "data/source/iedb_elution_assays.tsv")

#Bind assays

bind.assays = vroom("data/source/iedb_bind_assays.tsv",delim = "\t") %>% as.data.frame()
str(bind.assays)

# Критерии фильтрации:
# 1. У каждой записи должен быть источник
# 2. У каждой записи должен быть исход эксперимента
# 3. Последовательности должны быть записаны однобуквенным кодом для аминокислот
# 4. Эпитоп должен находиться в референсной последовательности
# 5. Эпитоп должен быть полным. Это не должен быть участок антигенного сайта или частью большего эпитопа.
# 6. Информация в комментариях не должна противоречить исходу эксперимента 
# 7. Результаты должны быть получены экспериментально, не быть предсказанными
# 8. Эпитопы должны быть иммуногенными
# 9. Не должно быть дефицита по TAP
# 10. Все HLA аллели должны быть записаны в формате, позволяющем однозначно идентифицировать молекулу

#1
sum(is.na(bind.assays$reference_id)) == 0
#2
sum(is.na(bind.assays$as_char_value)) == 0
#3
sum(grepl("[ACDEFGHIKLMNPQRSTVWYU]",bind.assays$linear_peptide_seq)) == nrow(bind.assays)
sum(grepl("[ACDEFGHIKLMNPQRSTVWYU]",bind.assays$linear_peptide_seq)) == nrow(bind.assays)
#4
ind = seq_len(nrow(bind.assays))
has.epitope = sapply(ind,function(i){
  grepl(bind.assays$linear_peptide_seq[i],bind.assays$sequence[i],fixed = T)
})
sum(has.epitope) == nrow(bind.assays)
#5
kable(round(table(bind.assays$e_region_domain_flag) / nrow(bind.assays) * 100,2),col.names = c("Value","Procent"))
#6
print(paste("Количество",length(unique(bind.assays$as_comments))))
print(paste("Количество записей",sum(!is.na(bind.assays$as_comments))))
write.table(unique(bind.assays$as_comments),"data/bind_assay_comments.txt")
kable(table(subset(bind.assays,grepl("did not",bind.assays$as_comments,fixed = T), select = "as_char_value")),caption = "did not")
kable(table(subset(bind.assays,grepl("bad",bind.assays$as_comments,fixed = T), select = "as_char_value")),caption = "bad")
#7
kable(table(subset(bind.assays,grepl("predict",bind.assays$as_comments,ignore.case = T), select = "as_char_value")),caption = "predict")
#8
kable(table(subset(bind.assays,grepl("non-immunogenic",bind.assays$as_comments,fixed = T), select = "as_char_value")),caption = "non-immunogenic")
#9
kable(table(subset(bind.assays,grepl("TAP-deficient",bind.assays$as_comments,fixed = T), select = "as_char_value")),caption = "TAP-deficient")
#10
sum(grepl("^HLA-[ABC]\\*\\d{2}:\\d{2}$",bind.assays$chain_i_name)) == nrow(bind.assays)

# Условия с 4 до 9 не соблюдаются. Исправим. Удалим все несовпадения, исправим противоречия, 
# исправим as_char_value
# Выделим результаты, полученные на TAP-дефицитном материале в отдельный датасет
print(paste("Количество строк до",dim(bind.assays)[1]))

bind.assays[grep("Positive", bind.assays$as_char_value),"as_char_value"] = "Positive"
bind.assays = bind.assays[has.epitope,]
bind.assays = bind.assays[grepl("Exact Epitope", bind.assays$e_region_domain_flag,fixed = T),]

bind.assays[grepl("bad",bind.assays$as_comments,fixed = T) & bind.assays$as_char_value == "Positive","as_char_value"] = 'Negative'
bind.assays[grepl("did not",bind.assays$as_comments,fixed = T) & bind.assays$as_char_value == "Positive","as_char_value"] = 'Negative'

tap.def = bind.assays[grepl("TAP-deficient",bind.assays$as_comments,fixed = T),]
vroom_write(tap.def,"data/tap_def_bind.csv")

bind.assays = bind.assays[!grepl("TAP-deficient",bind.assays$as_comments,fixed = T),]
bind.assays = bind.assays[!grepl("predict",bind.assays$as_comments,ignore.case = T),]
bind.assays = bind.assays[!grepl("non-immunogenic",bind.assays$as_comments,fixed = T),]

print(paste("Количество строк после",dim(bind.assays)[1]))

#Сохраним и будем дальше с ним работать
vroom_write(bind.assays, "data/source/iedb_bind_assays_clean.tsv")

#----------------------------------------------------------------