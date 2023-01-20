#Скрипт создает три отображения, содержащих данные о взаимодействии Т-клеточный эпитоп-MHC из БД IEDB.
#Необходимо для выполнения локальный MySQL сервер с разввернутой базой данных.
#Данные о таких взаимодействиях лежат в трех таблицах(три типа эксперемента): элюция лиганда и определение масс-спектрометрией(mhc_elution),
#определение связывания с помощью тетрамерных комплексов MHC(mhc_bind) и эксперименты с распозныванием Т-клетками эпитопов(tcell)
USE iedb;
#Создаем выборки таких экспериментов. Нам нужны человеческие MHC первого класса, положительные случаи
CREATE TABLE pos_human_mhcI_from_elution AS SELECT curated_epitope_id, mhc_allele_name, class FROM mhc_elution
    INNER JOIN mhc_allele_restriction mar ON mhc_elution.mhc_allele_restriction_id = mar.mhc_allele_restriction_id
    WHERE class = 'I' AND organism_ncbi_tax_id = 9606 AND as_char_value LIKE '%Positive%';

CREATE TABLE pos_human_mhcI_from_bind AS SELECT curated_epitope_id, mhc_allele_name, class FROM mhc_bind
    INNER JOIN mhc_allele_restriction mar ON mhc_bind.mhc_allele_restriction_id = mar.mhc_allele_restriction_id
    WHERE class = 'I' AND organism_ncbi_tax_id = 9606 AND as_char_value LIKE '%Positive%';

CREATE TABLE pos_human_mhcI_from_tcell AS SELECT curated_epitope_id, mhc_allele_name,class FROM tcell
    INNER JOIN mhc_allele_restriction mar ON tcell.mhc_allele_restriction_id = mar.mhc_allele_restriction_id
    WHERE class = 'I' AND organism_ncbi_tax_id = 9606 AND as_char_value LIKE '%Positive%';

#Создаем индексы для улучшения производительности, их можно создать только для таблицы(table)
CREATE INDEX epi_index ON pos_human_mhcI_from_elution (curated_epitope_id);
CREATE INDEX epi_index ON pos_human_mhcI_from_bind (curated_epitope_id);
CREATE INDEX epi_index ON pos_human_mhcI_from_tcell (curated_epitope_id);

#Создаем выборки таких экспериментов. Нам нужны человеческие MHC первого класса, положительные случаи
CREATE TABLE neg_human_mhcI_from_elution AS SELECT curated_epitope_id, mhc_allele_name, class FROM mhc_elution
    INNER JOIN mhc_allele_restriction mar ON mhc_elution.mhc_allele_restriction_id = mar.mhc_allele_restriction_id
    WHERE class = 'I' AND organism_ncbi_tax_id = 9606 AND as_char_value LIKE '%Negative%';

CREATE TABLE neg_human_mhcI_from_bind AS SELECT curated_epitope_id, mhc_allele_name, class FROM mhc_bind
    INNER JOIN mhc_allele_restriction mar ON mhc_bind.mhc_allele_restriction_id = mar.mhc_allele_restriction_id
    WHERE class = 'I' AND organism_ncbi_tax_id = 9606 AND as_char_value LIKE '%Negative%';

CREATE TABLE neg_human_mhcI_from_tcell AS SELECT curated_epitope_id, mhc_allele_name,class FROM tcell
    INNER JOIN mhc_allele_restriction mar ON tcell.mhc_allele_restriction_id = mar.mhc_allele_restriction_id
    WHERE class = 'I' AND organism_ncbi_tax_id = 9606 AND as_char_value LIKE '%Negative%';

#Создаем индексы для улучшения производительности, их можно создать только для таблицы(table)
CREATE INDEX epi_index ON neg_human_mhcI_from_elution (curated_epitope_id);
CREATE INDEX epi_index ON neg_human_mhcI_from_bind (curated_epitope_id);
CREATE INDEX epi_index ON neg_human_mhcI_from_tcell (curated_epitope_id);


#Вытаскиваем последовательности линейных эпитопов без модификаций, для которых известен референс и позиция в нем.
CREATE VIEW human_linear_epi AS
    SELECT curated_epitope_id,linear_peptide_seq, source_antigen_accession, source_organism_org_id, e_ref_start, e_ref_end FROM epitope
    INNER JOIN epitope_object eo ON epitope.epitope_id = eo.epitope_id
    INNER JOIN curated_epitope ce ON ce.e_object_id = eo.object_id
    WHERE linear_peptide_seq IS NOT NULL AND
          linear_peptide_modification IS NULL AND
          disc_source_id IS NULL AND
          linear_peptide_seq NOT LIKE '%[0-9]%' AND
          source_organism_org_id IS NOT NULL AND
          source_antigen_accession IS NOT NULL AND
          e_ref_end IS NOT NULL AND
          e_ref_start IS NOT NULL;


#Целевые подвыборки
CREATE VIEW pos_epi_elution AS SELECT hme.curated_epitope_id, linear_peptide_seq, source_antigen_accession, source_organism_org_id, e_ref_start, e_ref_end,
                                      hme.mhc_allele_name, hme.class from human_linear_epi
    INNER JOIN pos_human_mhcI_from_elution hme ON hme.curated_epitope_id = human_linear_epi.curated_epitope_id;

CREATE VIEW pos_epi_bind AS SELECT hmb.curated_epitope_id, linear_peptide_seq, source_antigen_accession, source_organism_org_id, e_ref_start, e_ref_end,
                                      hmb.mhc_allele_name, hmb.class from human_linear_epi
    INNER JOIN pos_human_mhcI_from_bind hmb ON hmb.curated_epitope_id = human_linear_epi.curated_epitope_id;

CREATE VIEW pos_epi_tcell AS SELECT hmt.curated_epitope_id, linear_peptide_seq, source_antigen_accession, source_organism_org_id, e_ref_start, e_ref_end,
                                      hmt.mhc_allele_name, hmt.class from human_linear_epi
    INNER JOIN pos_human_mhcI_from_tcell hmt ON hmt.curated_epitope_id = human_linear_epi.curated_epitope_id;

CREATE VIEW neg_epi_elution AS SELECT hme.curated_epitope_id, linear_peptide_seq, source_antigen_accession, source_organism_org_id, e_ref_start, e_ref_end,
                                      hme.mhc_allele_name, hme.class from human_linear_epi
    INNER JOIN neg_human_mhcI_from_elution hme ON hme.curated_epitope_id = human_linear_epi.curated_epitope_id;

CREATE VIEW neg_epi_bind AS SELECT hmb.curated_epitope_id, linear_peptide_seq, source_antigen_accession, source_organism_org_id, e_ref_start, e_ref_end,
                                      hmb.mhc_allele_name, hmb.class from human_linear_epi
    INNER JOIN neg_human_mhcI_from_bind hmb ON hmb.curated_epitope_id = human_linear_epi.curated_epitope_id;

CREATE VIEW neg_epi_tcell AS SELECT hmt.curated_epitope_id, linear_peptide_seq, source_antigen_accession, source_organism_org_id, e_ref_start, e_ref_end,
                                      hmt.mhc_allele_name, hmt.class from human_linear_epi
    INNER JOIN neg_human_mhcI_from_tcell hmt ON hmt.curated_epitope_id = human_linear_epi.curated_epitope_id;