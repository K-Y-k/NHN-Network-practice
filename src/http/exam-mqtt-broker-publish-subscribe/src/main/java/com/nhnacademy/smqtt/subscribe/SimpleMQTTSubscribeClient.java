package com.nhnacademy.smqtt.subscribe;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.nhnacademy.smqtt.utils.Utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SimpleMQTTSubscribeClient {
    static final String DEFAULT_BROCKER_HOST = "localhost";
    static final int DEFAULT_BROCKER_PORT = 1883;

    public static void main(String[] args) {
        Options options = new Options();

        options.addOption("h", "host", true, "host");
        options.addOption("p", "port", true, "port");
        options.addOption("c", "client_id", true, "client id");
        options.addOption(Option.builder("t").longOpt("topic").desc("Subscribe to topics").hasArgs().build());
        options.addOption("v", "verbose", false, "Verbose");

        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            String host = cmd.getOptionValue("h", DEFAULT_BROCKER_HOST);
            int port = Integer.parseInt(cmd.getOptionValue("p", String.valueOf(DEFAULT_BROCKER_PORT)));
            String clientId = cmd.getOptionValue("c");
            if (clientId == null) {
                clientId = Utils.createClientID();
            }
            String[] topics = cmd.getOptionValues("t");
            boolean verbose = cmd.hasOption("v");

            SubscribeClient client = new SubscribeClient(host, port, clientId);

            for (String topic : topics) {
                client.addTopic(topic);
            }

            client.setQoS(1);
            client.setVerbose(verbose);

            client.run();
        } catch (IOException e) {
            log.warn(e.getMessage());
        } catch (ParseException e) {
            log.warn(e.getMessage());
        }
    }
}