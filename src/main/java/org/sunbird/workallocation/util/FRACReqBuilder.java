package org.sunbird.workallocation.util;

import org.springframework.stereotype.Service;
import org.sunbird.workallocation.model.FracRequest;

@Service
public class FRACReqBuilder {
    public FracRequest getPositionRequest(String source, String name) {
        FracRequest req = new FracRequest();
        req.setName(source);
        req.setName(name);
        req.setType("POSITION");
        req.setId(null);
        return req;
    }
}
