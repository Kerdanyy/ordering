package com.foodics.ordering.config;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.FileInputStream;
import java.io.InputStream;

@Configuration
public class FirestoreConfig {

    private static final String TESTING_DATABASE_ID = "testing";

    @Bean
    @Profile("default")
    @SneakyThrows
    public Firestore firestore() {
        InputStream serviceAccount = new FileInputStream("./credentials.json");
        GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
        FirestoreOptions firestoreOptions = FirestoreOptions.newBuilder().setCredentialsProvider(FixedCredentialsProvider.create(credentials)).build();
        return firestoreOptions.getService();
    }

    @Bean
    @Profile("testing")
    @SneakyThrows
    public Firestore firestoreTesting() {
        InputStream serviceAccount = new FileInputStream("./credentials.json");
        GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
        FirestoreOptions firestoreOptions = FirestoreOptions.newBuilder().setCredentialsProvider(FixedCredentialsProvider.create(credentials)).setDatabaseId(TESTING_DATABASE_ID).build();
        return firestoreOptions.getService();
    }
}
