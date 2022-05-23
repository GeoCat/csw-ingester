package com.geocat.ingester.routes.rest;


import com.geocat.ingester.events.IngestEventService;
import com.geocat.ingester.routes.queuebased.IngestMainOrchestrator;
import org.apache.camel.BeanScope;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AbortIngest extends RouteBuilder {


    @Value("${app.jettyHost}")
    public String jettyHost;

    @Value("${app.jettyPort}")
    public Integer jettyPort;


    @Override
    public void configure() throws Exception {
        restConfiguration().component("jetty").host(jettyHost).port(jettyPort);

        // JacksonDataFormat jsonDefHarvesterConfig = new JacksonDataFormat(HarvesterConfig.class);

        //--- incoming start process request (HTTP)
        rest("/api/abort/")
                .post("/{processID}")
                .route()
                .routeId("rest.rest.abort")
                .setHeader("JMSCorrelationID", simple("${header.processID}"))

                .multicast()
                .to(ExchangePattern.InOnly, "direct:abort_addToQueue")

                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        exchange.getMessage().setHeader("content-type", "application/json");
                        exchange.getMessage().setBody("{\"status\": \"USERABORT in progress\"}");
                    }
                })
        ;

        // mini-route to send to the message queue
        from("direct:abort_addToQueue") // from above route
                .routeId("rest.AbortEvent")

                .log("REST - generating LinkCheckAbortEvent")
                .bean(IngestEventService.class, "createAbortEvent( ${header.processID} )", BeanScope.Request)
                .marshal().json() // convert HarvesterConfig back to json
                .to("activemq:" + IngestMainOrchestrator.myJMSQueueName) //send to message queue
        ;

    }
}
