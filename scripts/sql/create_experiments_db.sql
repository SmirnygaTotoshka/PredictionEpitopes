CREATE DATABASE experiments;
USE experiments;

CREATE TABLE Alphabet(
  id int NOT NULL AUTO_INCREMENT,
  name varchar(20) NOT NULL,
  PRIMARY KEY (id)
);

INSERT INTO Alphabet (name) VALUES ("DNA");
INSERT INTO Alphabet (name) VALUES ("RNA");
INSERT INTO Alphabet (name) VALUES ("protein");

CREATE TABLE ConverterConfig(
  id int NOT NULL AUTO_INCREMENT,
  input varchar(1000) NOT NULL,
  output varchar(1000) NOT NULL,
  `column` varchar(50) NOT NULL,
  charged bool,
  alphabet int,
  threads tinyint,
  `separator` char(1),
  filename varchar(50),
  delete_tmp bool,
  PRIMARY KEY (id),
  FOREIGN KEY (alphabet) REFERENCES Alphabet(id)
);

CREATE TABLE ModelConfig(
  id int NOT NULL AUTO_INCREMENT,
  workdir varchar(1000) NOT NULL,
  base_name varchar(100) NOT NULL,
  `data` varchar(100) NOT NULL,
  activity_column varchar(100) NOT NULL,
  exclude varchar(100),
  output varchar(100) NOT NULL,
  act_list_name varchar(100),
  PRIMARY KEY (id)
);

CREATE TABLE Models(
  id int NOT NULL AUTO_INCREMENT,
  experiment_date datetime NOT NULL,
  desc_level tinyint NOT NULL,
  method varchar(50) NOT NULL,
  model_name varchar(100) NOT NULL,
  converter_config int NOT NULL,
  model_config int NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (converter_config) REFERENCES ConverterConfig(id),
  FOREIGN KEY (model_config) REFERENCES ModelConfig(id)
);

CREATE TABLE Results(
  id int NOT NULL AUTO_INCREMENT,
  model_id int NOT NULL,
  success bool NOT NULL,
  train_size int NOT NULL,
  activity varchar(200) NOT NULL,
  iap float NOT NULL,
  fiveCV float,
  twentyCV float,
  looCV float,
  PRIMARY KEY (id),
  FOREIGN KEY (model_id) REFERENCES Models(id)
);

CREATE INDEX converter_config_index ON ConverterConfig (id);
CREATE INDEX model_config_index ON ModelConfig (id);
CREATE INDEX models_index ON Models (id, converter_config, model_config);
CREATE INDEX results_index ON Results (id, model_id);
