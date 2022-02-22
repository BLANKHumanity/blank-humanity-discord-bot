cat Dockerfile/head

projects=(discord-library blankdiscordbot-core funplace)

echo ""

for pr in ${projects[@]}
do
  if [ -n "$(ls -A build/$pr/dependency 2>/dev/null)" ]
  then 
    cat Dockerfile/dependencies | sed "s/{PROJECT}/$pr/g"   
  fi
done
 
echo ""

for pr in ${projects[@]}
do
  cat Dockerfile/jar | sed "s/{PROJECT}/$pr/g"
done

echo ""

cat Dockerfile/footer
