FROM ubuntu:16.04

COPY clj-holmes /bin/clj-holmes

RUN chmod +x /bin/clj-holmes
