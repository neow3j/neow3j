FROM docker:20.10.22-cli

RUN apk update
RUN apk add bash
RUN apk add jq
RUN apk add curl
RUN apk add expect
RUN apk add make
