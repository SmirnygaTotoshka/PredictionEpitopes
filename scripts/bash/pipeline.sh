#!/bin/bash
echo "Converter configs"
/home/stotoshka/Soft/anaconda3/envs/research/bin/python3 \
/home/stotoshka/Documents/Epitops/PredictionEpitopes/scripts/python/generateConfigs.py \
$1 0
echo "Execution configs"
/home/stotoshka/Soft/anaconda3/envs/research/bin/python3 \
/home/stotoshka/Documents/Epitops/PredictionEpitopes/scripts/python/generateConfigs.py \
$1 1
echo "Convert"
for entry in "$2"/*
do
  echo $entry
  /home/stotoshka/Soft/anaconda3/envs/research/bin/python3 \
  /home/stotoshka/Documents/Epitops/PredictionEpitopes/scripts/python/SeqToSDF.py \
  $entry
done
echo "Copy"
cp "$3"/*.sdf $4
echo "Finish"
