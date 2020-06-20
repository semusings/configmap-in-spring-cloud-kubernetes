DIR="$(pwd)"/.cache
mkdir -p "$DIR"

FILE=$DIR/linux-amd64/helm

if test -f "$FILE"; then
  echo "$FILE exist"
else
  echo "$FILE does not exist"
  curl -fsSL -o "$DIR"/helm.tar.gz https://get.helm.sh/helm-v3.2.3-linux-amd64.tar.gz
  cd "$DIR" && tar -xzvf helm.tar.gz && rm -rf helm.tar.gz && cd ..
fi

# shellcheck disable=SC2139
alias helm="$DIR/linux-amd64/helm"

printf '\n'
helm version
printf '\n'

DEPLOYMENT="example"

CHART_DIR="src/helm-chart"

option="${1}"
case ${option} in
   --deploy)
        helm upgrade \
        --install -f $CHART_DIR/values.yaml \
        $DEPLOYMENT $CHART_DIR --force
      ;;
   --update-charts)
        helm dependency update $CHART_DIR
      ;;
   --add-repos)
        helm repo add bitnami https://charts.bitnami.com/bitnami
      ;;
   --delete)
      helm delete $DEPLOYMENT
      ;;
   *)
      echo "`basename ${0}`:usage: [--deploy] | [--upgrade-charts] | [--add-repos] | [--delete]"
      exit 1 # Command to come out of the program with status 1
      ;;
esac
