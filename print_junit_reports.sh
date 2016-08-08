#!/usr/bin/env sh
echo "Current directory is $(pwd)"
echo "\n=== JUNIT REPORTS ===\n"
for F in /home/travis/build/Netflix/karyon/karyon2-jersey-blocking/build/reports/tests/*.html
do
    echo $F
    cat $F
    echo
done
