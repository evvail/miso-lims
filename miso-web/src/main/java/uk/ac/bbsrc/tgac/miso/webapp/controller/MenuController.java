/*
 * Copyright (c) 2012. The Genome Analysis Centre, Norwich, UK
 * MISO project contacts: Robert Davey @ TGAC
 * *********************************************************************
 *
 * This file is part of MISO.
 *
 * MISO is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MISO is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MISO. If not, see <http://www.gnu.org/licenses/>.
 *
 * *********************************************************************
 */

package uk.ac.bbsrc.tgac.miso.webapp.controller;

import java.io.IOException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponentsBuilder;

import com.eaglegenomics.simlims.core.User;
import com.eaglegenomics.simlims.core.manager.SecurityManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import uk.ac.bbsrc.tgac.miso.core.data.IndexFamily;
import uk.ac.bbsrc.tgac.miso.core.data.SampleValidRelationship;
import uk.ac.bbsrc.tgac.miso.core.data.type.PlatformType;
import uk.ac.bbsrc.tgac.miso.core.manager.RequestManager;
import uk.ac.bbsrc.tgac.miso.core.service.IndexService;
import uk.ac.bbsrc.tgac.miso.dto.Dtos;
import uk.ac.bbsrc.tgac.miso.dto.SampleClassDto;
import uk.ac.bbsrc.tgac.miso.dto.WritableUrls;
import uk.ac.bbsrc.tgac.miso.integration.util.SignatureHelper;
import uk.ac.bbsrc.tgac.miso.service.DetailedQcStatusService;
import uk.ac.bbsrc.tgac.miso.service.KitService;
import uk.ac.bbsrc.tgac.miso.service.LibraryService;
import uk.ac.bbsrc.tgac.miso.service.SampleClassService;
import uk.ac.bbsrc.tgac.miso.service.SampleGroupService;
import uk.ac.bbsrc.tgac.miso.service.SampleValidRelationshipService;
import uk.ac.bbsrc.tgac.miso.service.SubprojectService;
import uk.ac.bbsrc.tgac.miso.webapp.util.MisoWebUtils;

@Controller
public class MenuController implements ServletContextAware {
  protected static final Logger log = LoggerFactory.getLogger(MenuController.class);

  private String constantsJs;
  private long constantsJsTime;

  ServletContext servletContext;
  @Autowired
  private SecurityManager securityManager;
  @Autowired
  private RequestManager requestManager;
  @Autowired
  private KitService kitService;
  @Autowired
  private IndexService indexService;
  @Autowired
  private SampleClassService sampleClassService;
  @Autowired
  private DetailedQcStatusService detailedQcStatusService;

  @Autowired
  private LibraryService libraryService;
  @Autowired
  private SampleValidRelationshipService sampleValidRelationshipService;
  @Autowired
  private SubprojectService subprojectService;

  @Autowired
  private SampleGroupService sampleGroupService;

  @Value("${miso.autoGenerateIdentificationBarcodes}")
  private Boolean autoGenerateIdBarcodes;
  @Value("${miso.detailed.sample.enabled}")
  private Boolean detailedSample;

  @ModelAttribute("autoGenerateIdBarcodes")
  public Boolean autoGenerateIdentificationBarcodes() {
    return autoGenerateIdBarcodes;
  }

  @ModelAttribute("detailedSample")
  public Boolean isDetailedSampleEnabled() {
    return detailedSample;
  }

  @RequestMapping("/tech/menu")
  public String techMenu() {
    return "/pages/techMenu.jsp";
  }

  @RequestMapping("/admin/menu")
  public String adminMenu() {
    return "/pages/adminMenu.jsp";
  }

  @RequestMapping("/custombarcode")
  public String custombarcode() {
    return "/pages/customBarcodePrinting.jsp";
  }

  @RequestMapping("/myAccount")
  public ModelAndView myAccountMenu(ModelMap model) {
    try {
      User user = securityManager.getUserByLoginName(SecurityContextHolder.getContext().getAuthentication().getName());
      String realName = user.getFullName();
      StringBuilder groups = new StringBuilder();
      for (String role : user.getRoles()) {
        groups.append(role.replaceAll("ROLE_", "") + "&nbsp;");
      }
      model.put("title", "My Account");
      model.put("userRealName", realName);
      model.put("userId", user.getUserId());
      model.put("apiKey", SignatureHelper.generatePrivateUserKey((user.getLoginName() + "::" + user.getPassword()).getBytes("UTF-8")));
      model.put("userGroups", groups.toString());
      return new ModelAndView("/pages/myAccount.jsp", model);
    } catch (IOException e) {
      log.error("my account menu", e);
      return new ModelAndView("/login.jsp", model);
    } catch (NoSuchAlgorithmException e) {
      log.error("my account menu", e);
      return new ModelAndView("/login.jsp", model);
    }
  }

  public void setSecurityManager(com.eaglegenomics.simlims.core.manager.SecurityManager securityManager) {
    this.securityManager = securityManager;
  }

  @RequestMapping("/mainMenu")
  public ModelAndView mainMenu(ModelMap model) {
    try {
      User user = securityManager.getUserByLoginName(SecurityContextHolder.getContext().getAuthentication().getName());
      model.put("title", "Home");
      Map<String, String> checks = MisoWebUtils.checkStorageDirectories((String) servletContext.getAttribute("miso.baseDirectory"));
      if (checks.keySet().contains("error")) {
        model.put("error", checks.get("error"));
      }
      if (Arrays.asList(user.getRoles()).contains("ROLE_EXTERNAL") && !Arrays.asList(user.getRoles()).contains("ROLE_INTERNAL")) {
        return new ModelAndView("/pages/external/externalMain.jsp", model);
      } else {
        return new ModelAndView("/pages/mainMenu.jsp", model);
      }
    } catch (IOException e) {
      return new ModelAndView("/login.jsp", model);
    }
  }

  @RequestMapping("/projectMenu")
  public String projectMenu() {
    return "/pages/projectMenu.jsp";
  }

  @RequestMapping("/activity/menu")
  public String activityMenu() {
    return "/pages/activityMenu.jsp";
  }

  @RequestMapping("/admin/instituteDefaults")
  public ModelAndView tissueOptions(ModelMap model) {
    model.put("title", "Institute Defaults");
    return new ModelAndView("/pages/instituteDefaults.jsp", model);
  }

  @Override
  public void setServletContext(ServletContext servletContext) {
    this.servletContext = servletContext;
  }

  private static <Model, Dto> void createArray(ObjectMapper mapper, URI baseUri, ObjectNode node, String name, Iterable<Model> items,
      Function<Model, Dto> asDto) {
    ArrayNode array = node.putArray(name);
    for (Model item : items) {
      Dto dto = asDto.apply(item);
      if (dto instanceof WritableUrls) {
        ((WritableUrls) dto).writeUrls(baseUri);
      }
      JsonNode itemNode = mapper.valueToTree(dto);
      array.add(itemNode);
    }
  }

  @RequestMapping(path = "/constants.js")
  @ResponseBody
  public String constantsScript(HttpServletResponse response, final UriComponentsBuilder uriBuilder) throws IOException {
    response.setContentType("application/javascript");
    // Use a cached copy and only update every
    if (constantsJs != null && (System.currentTimeMillis() - constantsJsTime) < 15 * 60 * 1000) {
      return constantsJs;
    }
    URI baseUri = uriBuilder.build().toUri();
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode node = mapper.createObjectNode();
    node.put("isDetailedSample", isDetailedSampleEnabled());
    node.put("automaticBarcodes", autoGenerateIdentificationBarcodes());

    final Iterable<SampleValidRelationship> relationships = sampleValidRelationshipService.getAll();

    createArray(mapper, baseUri, node, "libraryDesigns", requestManager.listLibraryDesigns(), Dtos::asDto);
    createArray(mapper, baseUri, node, "libraryTypes", libraryService.listLibraryTypes(), Dtos::asDto);
    createArray(mapper, baseUri, node, "librarySelections", libraryService.listLibrarySelectionTypes(), Dtos::asDto);
    createArray(mapper, baseUri, node, "libraryStrategies", libraryService.listLibraryStrategyTypes(), Dtos::asDto);
    createArray(mapper, baseUri, node, "libraryDesignCodes", requestManager.listLibraryDesignCodes(), Dtos::asDto);
    createArray(mapper, baseUri, node, "platforms", requestManager.listAllPlatforms(), Dtos::asDto);
    createArray(mapper, baseUri, node, "kitDescriptors", kitService.listKitDescriptors(), Dtos::asDto);
    createArray(mapper, baseUri, node, "sampleClasses", sampleClassService.getAll(), model -> {
      SampleClassDto dto = Dtos.asDto(model);
      dto.setCanCreateNew(model.canCreateNew(relationships));
      return dto;
    });
    createArray(mapper, baseUri, node, "sampleValidRelationships", relationships, Dtos::asDto);
    createArray(mapper, baseUri, node, "detailedQcStatuses", detailedQcStatusService.getAll(), Dtos::asDto);
    createArray(mapper, baseUri, node, "sampleGroups", sampleGroupService.getAll(), Dtos::asDto);
    createArray(mapper, baseUri, node, "subprojects", subprojectService.getAll(), Dtos::asDto);

    Collection<IndexFamily> indexFamilies = indexService.getIndexFamilies();
    indexFamilies.add(IndexFamily.NULL);
    createArray(mapper, baseUri, node, "indexFamilies", indexFamilies, Dtos::asDto);

    ArrayNode platformTypes = node.putArray("platformTypes");
    Collection<PlatformType> activePlatformTypes = requestManager.listActivePlatformTypes();
    for (PlatformType platformType : PlatformType.values()) {
      ObjectNode dto = platformTypes.addObject();
      dto.put("name", platformType.name());
      dto.put("key", platformType.getKey());
      dto.put("containerName", platformType.getContainerName());
      dto.put("active", activePlatformTypes.contains(platformType));
    }

    // Save the regenerated file in cache. This has a race condition where multiple concurrent requests could results in regenerating this
    // file and updating the cache. Since the cache is two variables (data and time), they can also be torn. Given the nature of the cached
    // data, we don't really care since the results are probably the same and jitter of a few seconds is a small error in cache time.
    constantsJs = "Constants = " + mapper.writeValueAsString(node) + ";";
    constantsJsTime = System.currentTimeMillis();
    return constantsJs;
  }
}
