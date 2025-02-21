package com.nhnacademy.smqtt.publish;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.nhnacademy.smqtt.utils.Utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SimpleMQTTPublishClient {
    static final String DEFAULT_BROCKER_HOST = "localhost";
    static final int DEFAULT_BROCKER_PORT = 1883;

    public static void main(String[] args) {
        Options options = new Options();

        options.addOption("h", "host", true, "host");
        options.addOption("p", "port", true, "port");
        options.addOption("c", "client_id", true, "client id");
        options.addOption(Option.builder("t").longOpt("topic").hasArg().build());
        options.addOption(Option.builder("m").longOpt("message").hasArg().build());

        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            String host = cmd.getOptionValue("h", DEFAULT_BROCKER_HOST);
            int port = Integer.parseInt(cmd.getOptionValue("p", String.valueOf(DEFAULT_BROCKER_PORT)));
            String clientId = cmd.getOptionValue("c");
            if (clientId == null) {
                clientId = Utils.createClientID();
            }
            String topic = cmd.getOptionValue("t");

            InputStream inputStream;
            if (cmd.hasOption("m")) {
                String message = cmd.getOptionValue("m");
                inputStream = new ByteArrayInputStream(message.getBytes());
            } else {
                inputStream = System.in;
            }

            PublishClient client = new PublishClient(host, port, clientId, topic, inputStream);
            client.setQoS(1);

            client.run();

        } catch (IOException e) {
            log.warn(e.getMessage());
        } catch (ParseException ignore) {
            System.err.println("인수가 잘못되었습니다.");
        }
    }
}
