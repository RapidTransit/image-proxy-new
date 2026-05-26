#!/bin/fish

#read ID --prompt-str="Input Id> "
#read SECRET --prompt-str="Input Secret> "
#read JWTKEY --prompt-str="Input JWT Key> "
#
source ./export.secret.sh
echo $ID
set REQUEST $(printf '{"clientId":"%s","clientSecret":"%s"}' $ID $SECRET)

set BEARER_TOKEN $(curl --request POST \
  --url https://api.sirv.com/v2/token \
  --header 'content-type: application/json' \
  --data $REQUEST | jq -r .token)
echo "authorization: Bearer $BEARER_TOKEN"
echo $BEARER_TOKEN
set my_profiles p ps-m_m ps-m_l ps-l ps-l_m ps-l_l p-t p-t_m p-t_l
set my_profiles_ins p p-m_m p-m_l p-l p-l_m p-l_l p-t p-t_m p-t_l
set my_widths 250 500 750 1000 2000 3000 50 100 150

for i in (seq 1 9)
  echo $my_profiles[$i]
  echo $my_widths[$i]
  set result $(curl -sS --request POST \
    --url https://api.sirv.com/v2/files/jwt \
    --header "authorization: Bearer $BEARER_TOKEN" \
    --header 'content-type: application/json' \
    --data $(printf '{"filename":"/p/","key":"%s","alias":"pleasant-smoke","secureParams":{"w":%d, "thumbnail": %d, "profile": "%s"},"expiresIn":15552000}' $JWTKEY $my_widths[$i] $my_widths[$i] $my_profiles[$i]) | jq -r .url )
     set split = $(string split = $result )
     set JWT[$i]  $(printf '    %s: "%s"' $my_profiles_ins[$i] $split[3])
end


echo -e "proxy:\n  jwt-mappings:\n"$( string join "\n" $JWT) > application-jwt.yaml