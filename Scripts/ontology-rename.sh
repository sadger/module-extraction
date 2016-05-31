#! /bin/zsh
files=`ls | grep "^[0-9]"` #Unprocessed files
renamed_folder="./renamed/"
while read -r line; do
		echo -e "\e[32m $line \e[0m" #Nice green ontology name
		echo -e "\n"
		echo "Suggestions:"
		cat $line | grep -oh "\w*\.owl" | sort | uniq
		echo -e "\n"

		DONE=0
		until [  $DONE = 1 ]; do
			vared -p '[E]nter Name, [H]ead file, [S]kip: ' -c tmp
			if [ "$tmp" = "e" ]; then
				vared -p 'Name: ' -c name
				new_location="$renamed_folder/$name"
        if [ -f $new_location ]; then
          echo "NAME ALREADY EXISTS"
        else
          echo "$line,$name" >> $renamed_folder/rename-list.txt
          mv $line $renamed_folder/$name
          DONE=1
        fi
        name=''
			elif [ "$tmp" = "h" ]; then
				cat $line | less
			elif [ "$tmp" = "s" ]; then
				echo "Skip"
				DONE=1
			fi;
			tmp=""
		done

done <<< "$files"
