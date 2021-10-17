package me.ghwn.netflix.accountservice.controller;

import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith({RestDocumentationExtension.class})
@SpringBootTest
class IndexControllerTest {

    @Autowired WebApplicationContext webApplicationContext;

    RestDocumentationResultHandler documentHandler;
    MockMvc mockMvc;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) {
        documentHandler = document(
                "{method-name}",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint())
        );

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(restDocumentation))
                .alwaysDo(print())
                .alwaysDo(documentHandler)
                .build();
    }

    @Test
    @DisplayName("Get links of all available resources")
    void index() throws Exception {
        mockMvc.perform(get("/api"))
                .andExpect(status().isOk())
                .andExpect(header().stringValues(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
                .andExpect(jsonPath("_links.profile.href").exists())
                .andExpect(jsonPath("_links.accounts.href").exists())

                .andDo(documentHandler.document(
                        responseFields(
                                fieldWithPath("_links.profile.href").description("Link to document"),
                                fieldWithPath("_links.accounts.href").description("Link to <<resources_accounts, accounts resource>>")
                        ),
                        links(
                                linkWithRel("profile").description("Link to document"),
                                linkWithRel("accounts").description("Link to <<resources_accounts, accounts resource>>")
                        )
                ));
    }

    @Test
    @DisplayName("Try to access invalid URI")
    @Disabled
    void accessInvalidUri() throws Exception {
        mockMvc.perform(get("/api/v1/no-resources"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("errors[*].message").exists())
                .andExpect(jsonPath("_links.index.href").exists())

                .andDo(documentHandler.document(
                        responseFields(
                                fieldWithPath("errors[].message").description("Error message"),
                                fieldWithPath("_links.index.href").description("Link to <<resources_index, index resource>>")
                        ),
                        links(
                                linkWithRel("index").description("Link to <<resources_index, index resource>>")
                        )
                ));
    }
}
