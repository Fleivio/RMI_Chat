# java -cp build/jars/ServerChat.jar:build/jars/UserChat.jar server.ServerChat

java -cp build/classes \
     -Djava.rmi.server.hostname=10.1.1.230 \
     -Djava.security.policy=rmi.policy \
     server.ServerChat