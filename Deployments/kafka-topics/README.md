# Einspielen neuer Topics


```
 kafkacli updateTopics -s VRZ -u C2VBA (--replicationFactorOverwrite 1) (--delete-topics) (--dry-run) c2vba-topics.csv
```

**--delete-topics**: Ist diese Flagge gesetzt, werden die Topics gelöscht, die in der **c2vba-topics.csv kein** Kreuzchen haben.

**--dry-run**: Mit dieser Flagge wird nur ausgegeben, was geändert würde. Es werden aber **keine** Änderungen durchgeführt.


# Aufruf unter Windows

kafkacli ist aus einer git-bash so aufzurufen:

**winpty ./kafkacli updateTopics -s VRZ -u C2VBA ...**
