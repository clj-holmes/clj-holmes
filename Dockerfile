FROM ubuntu:resolute-20260413

RUN apt-get update && apt-get upgrade -y
COPY clj-holmes-ubuntu-latest /bin/clj-holmes

RUN chmod +x /bin/clj-holmes
