package com.nhnacademy.smqtt.broker;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SimpleMQTTBroker {
    static final String DEFAULT_PORT = "1883";

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption("h", "host", true, "host");
        options.addOption("p", "port", true, "port");

        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            int port = Integer.parseInt(cmd.getOptionValue("p", DEFAULT_PORT));

            Broker broker = new Broker(port);

            broker.start();
        } catch (ParseException e) {
            System.err.println("인수가 잘못되었습니다.");
        }
    }

}
