//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.wrapper.Services;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wrapper.ResponseController.OnConfirmResponse;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import org.springframework.http.ResponseEntity;

public interface AddCareContext {
	ResponseEntity<ObjectNode> addContext(OnConfirmResponse data) throws URISyntaxException, FileNotFoundException;
}
