#!/bin/bash


toBinary(){
    local n bits sign=''
    (($1<0)) && sign=-
    for (( n=$sign$1 ; n>0 ; n >>= 1 )); do bits=$((n&1))$bits; done
    #printf "%s\n" "$sign${bits-0}"
    echo "$sign${bits-0}"
}

for i in {1..6}
do
   toBinary i > preset.txt
   
done
./gradlew run >> ran.txt