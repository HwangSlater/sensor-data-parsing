package com.sensor_data_parsing;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttPublisher {
    private static final String DEFAULT_BROKER = "tcp://localhost:8888"; // 브로커 URL
    private static final String DEFAULT_TOPIC = "test/topic"; // 메시지를 보낼 토픽
    private static final String DEFAULT_CLIENTID = "Publisher"; // 클라이언트 ID

    public static void main(String[] args) {
        try (MqttClient client = new MqttClient(DEFAULT_BROKER, DEFAULT_CLIENTID);) {
            // 브로커에 연결
            client.connect();

            // 전송할 메시지 생성
            String messageContent = "Hello, MQTT!"; // 전송할 메시지 내용
            MqttMessage message = new MqttMessage(messageContent.getBytes());

            // 메시지 전송
            client.publish(DEFAULT_TOPIC, message);
            System.out.println("Message published: " + messageContent);

            // 연결 종료
            client.disconnect();
            System.out.println("Disconnected from the broker.");
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
