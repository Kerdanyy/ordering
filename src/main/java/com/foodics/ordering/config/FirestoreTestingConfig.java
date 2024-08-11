package com.foodics.ordering.config;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.InputStream;

@Configuration
public class FirestoreTestingConfig {

    private static final String TESTING_DATABASE_ID = "testing";

    @Bean
    @Qualifier("firestoreTesting")
    @SneakyThrows
    public Firestore firestoreTesting() {
        InputStream serviceAccount = new FileInputStream("./credentials.json");
        GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
        FirestoreOptions firestoreOptions = FirestoreOptions.newBuilder().setCredentialsProvider(FixedCredentialsProvider.create(credentials)).setDatabaseId(TESTING_DATABASE_ID).build();
        return firestoreOptions.getService();
    }
}
