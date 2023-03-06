#!/bin/bash

rsync -rvc --human-readable --delete --progress --partial --exclude-from=exclude_from_backup.txt --delete-excluded --timeout=300 /home/stotoshka/Documents/Epitops/PredictionEpitopes /run/user/1000/gvfs/smb-share:server=tp-share,share=g/Diplom
