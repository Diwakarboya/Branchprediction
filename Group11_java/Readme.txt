Use the 'make' command to compile the code.
Then we can use the below commands to execute the simualtion and copy the output to file to check the difference using the diff command.

--- Smith ---
java sim smith 3 gcc_trace.txt > smith_output.txt    (diff -i -w  smith_output.txt val_smith_1.txt)
java sim smith 1 jpeg_trace.txt > smith_output.txt   (diff -i -w  smith_output.txt val_smith_2.txt)
java sim smith 4 perl_trace.txt > smith_output.txt   (diff -i -w  smith_output.txt val_smith_3.txt)

--- Bimodal ---
java sim bimodal 6 gcc_trace.txt > bimodal_output.txt    (diff -i -w  bimodal_output.txt val_bimodal_1.txt)
java sim bimodal 12 gcc_trace.txt > bimodal_output.txt   (diff -i -w  bimodal_output.txt val_bimodal_2.txt)
java sim bimodal 4 jpeg_trace.txt > bimodal_output.txt   (diff -i -w  bimodal_output.txt val_bimodal_3.txt)


--- Gshare ---
java sim gshare 9 3 gcc_trace.txt > gshare_output.txt     (diff -i -w  gshare_output.txt val_gshare_1.txt)
java sim gshare 14 8 gcc_trace.txt > gshare_output.txt    (diff -i -w  gshare_output.txt val_gshare_2.txt)
java sim gshare 11 5 jpeg_trace.txt > gshare_output.txt   (diff -i -w  gshare_output.txt val_gshare_3.txt)


--- Hybrid ---
java sim hybrid 8 14 10 5 gcc_trace.txt > hybrid_output.txt   (diff -i -w  hybrid_output.txt val_hybrid_1.txt)

