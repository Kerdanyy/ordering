package com.foodics.ordering.config;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.InputStream;

@Configuration
public class FirestoreConfig {

    @Bean
    @SneakyThrows
    public Firestore firestore() {
        InputStream serviceAccount = new FileInputStream("./credentials.json");
        GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
        FirestoreOptions firestoreOptions = FirestoreOptions.newBuilder().setCredentialsProvider(FixedCredentialsProvider.create(credentials)).build();
        return firestoreOptions.getService();
    }
}
