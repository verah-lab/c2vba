# Setzt den default NAMESPACE, wenn in den apply.sh Aufrufen
# nicht als erster Parameter explizit etwas anderes angeben wird.
# die namespace.sh soll von allen apply*.sh Skripten includiert werden.
# Die kubectl Befehle sollen alle mit "-n ${NAMESPACE} erfolgen
# Die Deployment yamls sollen keinen namespace mehr enthalten
NAMESPACE=c2vba
export NAMESPACE
