package me.ghwn.netflix.accountservice.controller;

import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.MediaTypes;

import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class IndexControllerTest extends BaseControllerTest {

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
