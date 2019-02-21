FROM rabbitmq:3.6.6-management
MAINTAINER Anonymous

#ADD rabbitmq.config /etc/rabbitmq/
#ADD definitions.json /etc/rabbitmq/
#RUN chown rabbitmq:rabbitmq /etc/rabbitmq/rabbitmq.config /etc/rabbitmq/definitions.json
#CMD ["rabbitmq-server"]
ENV RABBITMQ_USER user
ENV RABBITMQ_PASSWORD user

ADD init-server.sh /init.sh
RUN chmod u+x /init.sh
CMD ["/init.sh"]
