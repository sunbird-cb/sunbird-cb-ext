package org.sunbird.storage.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(includeFieldNames = true)
@AllArgsConstructor
@NoArgsConstructor

@SuppressWarnings("all")
public class StorageService {//storage service config info

    private int id;
    private String provider = "azure";
    private String container="sb-cb-ext";
    private String identity= "4WCx35Qb3qnQOgkrkjkOz6shiJ264idVRFJc6HErwfyaR5V/weC7n+7tW4bz5NdcEifWeVT3W9dmZxTokZ7uAw==";
    private String credential="sb-cb-ext";

}

