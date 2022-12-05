#!/usr/bin/env bash

if [[ -z "${CREDENTIALS}" ]]; then
  echo "CREDENTIALS environment variable isn't set and the script won't be able to auth to Nexus"
  echo "Run the following command with your credentials and then try this again"
  echo "    export CREDENTIALS={{username}}:{{password}}"
  exit -1
fi

# Thanks to this SO post for the help: https://stackoverflow.com/a/11198713
end=$((SECONDS+300))

count=0
while [ $SECONDS -lt $end ]; do
    curl -u $CREDENTIALS \
      -X 'POST' \
      'http://localhost:8081/service/rest/v1/components?repository=npm-evaluation' \
      -H 'accept: application/json' \
      -H 'Content-Type: multipart/form-data' \
      -H 'NX-ANTI-CSRF-TOKEN: 0.6265488697033319' \
      -F 'npm.asset=@william-rowan-hamilton-1.0.2.tgz;type=application/gzip'
      echo "Uploaded!  Waiting..."
      sleep 5
      curl -X 'DELETE' 'http://localhost:8080/admin/clear?repo=npm-evaluation'
      echo "Deleted."

      ((count=count+1))
done
echo "Ran $count times."
