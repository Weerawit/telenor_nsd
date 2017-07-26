package com.nokia.cloudband.cbnd.nsdgenerator.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nokia.cloudband.cbnd.nsdgenerator.util.TemplateUtil;
import com.nokia.cloudband.cbnd.nsdgenerator.util.ZipUtil;

public abstract class GenericController {

	protected Log log = LogFactory.getLog(getClass());

	@SuppressWarnings("unchecked")
	protected Map<String, String> convertToMap(String arrayJson) throws JsonParseException, JsonMappingException, IOException {
		Map<String, String> results = new HashMap<String, String>();
		if (StringUtils.isNotEmpty(arrayJson)) {
			ArrayList<Map<String, String>> objs = new ObjectMapper().readValue(arrayJson, ArrayList.class);
			for (int i = 0; i < objs.size(); i++) {
				Map<String, String> object = objs.get(i);
				results.put(object.get("name"), object.get("value"));
			}
		}
		return results;
	}

	@RequestMapping(path = "/download", method = RequestMethod.GET, produces = "application/zip")
	public void download(HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession();

		response.setContentType("application/zip");
		response.setStatus(HttpServletResponse.SC_OK);
		response.addHeader("Content-Disposition", "attachment; filename=\"" + (String) session.getAttribute("mainYamlFileName") + ".zip\"");

		File zipFile = new File((String) session.getAttribute("file"));

		FileInputStream in = null;
		try {
			in = new FileInputStream(zipFile);
			IOUtils.copy(in, response.getOutputStream());
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(path = "/generate", method = RequestMethod.POST)
	public ResponseEntity<?> generate(@RequestBody String request, HttpSession session) {
		try {
			Map<String, String> requestObj = new ObjectMapper().readValue(request, HashMap.class);

			Map<String, String> metadataObj = convertToMap(requestObj.get("metadata"));

			ArrayList<Map<String, String>> networkObjList = new ObjectMapper().readValue(requestObj.get("networks"), ArrayList.class);

			ArrayList<Map<String, String>> vnfObjList =  new ObjectMapper().readValue(requestObj.get("vnf"), ArrayList.class);

			Map<String, String> nuageObj = convertToMap(requestObj.get("nuage"));

			List<String> placements = new ArrayList<String>();

			// prepare yaml folder
			String metaName = metadataObj.get("meta_name");
			String metaVersion = metadataObj.get("meta_version");

			String mainYamlFileName = metaName + "_" + metaVersion + ".yaml";

			// create metadata file
			String metadataContent = TemplateUtil.generateMetadata(metaName, metaVersion, metadataObj.get("description"), mainYamlFileName);

			// prepare input param
			// vnf
			String vnfInput = "";
			for (Map<String, String> vnf : vnfObjList) {
				Map<String, String> vnfObj = convertToMap(vnf.get("value"));
				
				if ("huawei".equalsIgnoreCase(vnfObj.get("vnf_type"))) {
					String vnfm_h_name = vnfObj.get("vnfm_h_name");
					String vnfm_h_id = vnfObj.get("vnfm_h_id");
					String vnfm_h_vnfd_id_in_blueprint = vnfObj.get("vnfm_h_vnfd_id_in_blueprint");
					String vnfm_h_plan_name = vnfObj.get("vnfm_h_plan_name");
					String vnfm_h_vendor = vnfObj.get("vnfm_h_vendor");
					String vnfm_h_vnfd_version = vnfObj.get("vnfm_h_vnfd_version");
					vnfInput += TemplateUtil.generateInputVnfHuawei(vnfm_h_name, vnfm_h_id, vnfm_h_vnfd_id_in_blueprint, vnfm_h_plan_name, vnfm_h_vendor, vnfm_h_vnfd_version);

				} else if ("zte".equalsIgnoreCase(vnfObj.get("vnf_type"))) {
					throw new RuntimeException("zte not imple");
				} else {
					throw new RuntimeException("cbam not imple");
				}
			}
			
			// network
			String networkInput = "";
			for (Map<String, String> network : networkObjList) {
				Map<String, String> networkData = convertToMap(network.get("value"));
				String nw_type = networkData.get("nw_type");
				if ("external".equalsIgnoreCase(nw_type)) {

					networkInput += generateInputExternalNetwork(networkData);

				} else if ("internal".equalsIgnoreCase(nw_type)) {
					String nw_int_name = networkData.get("nw_int_name");
					String nw_int_cidr = networkData.get("nw_int_cidr");
					String nw_int_dhcp = networkData.get("nw_int_dhcp");

					networkInput += TemplateUtil.generateInputInternalNetwork(nw_int_name, nw_int_cidr, nw_int_dhcp);

				} else {
					// predefine
					String nw_p_name = networkData.get("nw_p_name");
					String nw_p_uuid = networkData.get("nw_p_uuid");

					networkInput += TemplateUtil.generateInputPredefineNetwork(nw_p_name, nw_p_uuid);

				}
			}
			// nuage
			String nuageInput = generateNuageInput(nuageObj);

			String input = vnfInput + networkInput + nuageInput;

			// node_template
			String node_template = "";
			for (Map<String, String> vnf : vnfObjList) {
				Map<String, String> vnfObj = convertToMap(vnf.get("value"));
				
				String connection_point = "";
				String requirement = "";
				
				boolean foundRequirement = false;
				
				if (StringUtils.isNotBlank(vnfObj.get("vnf_depend"))) {
					foundRequirement = true;
					
					requirement += TemplateUtil.generateVnfRequirementDepend(vnfObj.get("vnf_depend"));
				}
				
				
				if ("huawei".equalsIgnoreCase(vnfObj.get("vnf_type"))) {
					String vnf_name = vnfObj.get("vnfm_h_name");

					Set<String> keys = vnfObj.keySet();
					Iterator<String> keyIterator = keys.iterator();
					while (keyIterator.hasNext()) {
						String key = keyIterator.next();
						if (key.startsWith("connection_point_name_")) {
							foundRequirement = true;
							String uuid = StringUtils.splitByWholeSeparator(key, "connection_point_name_")[0];
							String cp_name = vnfObj.get("connection_point_name_" + uuid);
							String nw_name = vnfObj.get("connection_point_mapping_" + uuid);

							String nw_type = null;
							for (Map<String, String> network : networkObjList) {
								Map<String, String> networkData = convertToMap(network.get("value"));
								String toFindNwName = networkData.get("nw_ext_name");
								if (StringUtils.equalsIgnoreCase(nw_name, toFindNwName)) {
									nw_type = networkData.get("nw_type");
									break;
								}
							}

							connection_point += TemplateUtil.generateVnfHuaweiConnectinPoint(cp_name, nw_name, nw_type, getNetworkAttributeId());

							requirement += TemplateUtil.generateVnfRequirementVL(cp_name, nw_name);

							requirement += TemplateUtil.generateVnfRequirementSubnet(cp_name, nw_name);
						}
					}
					
					if (foundRequirement) {
						requirement = "    requirements:\n" + requirement;
					}

					node_template += TemplateUtil.generateVnfHuawei(vnf_name, connection_point, requirement);

					placements.add(vnf_name + "_vnf");
					

				} else if ("zte".equalsIgnoreCase(vnfObj.get("vnf_type"))) {
					throw new RuntimeException("zte not imple");
				} else {
					throw new RuntimeException("cbam not imple");
				}
			}

			// implementation_model
			String virtual_link = "";
			String implementation_model = generateBasicImplementationModel(nuageObj, placements);
			for (Map<String, String> network : networkObjList) {
				Map<String, String> networkData = convertToMap(network.get("value"));
				String nw_type = networkData.get("nw_type");
				if ("external".equalsIgnoreCase(nw_type)) {
					String nw_ext_name = networkData.get("nw_ext_name");

					implementation_model += generateImplModelExternalNetwork(networkData, placements);

					virtual_link += TemplateUtil.generateVnfVL(nw_ext_name);

				} else if ("internal".equalsIgnoreCase(nw_type)) {
					String nw_int_name = networkData.get("nw_int_name");

					implementation_model += generateImplModelInternalNetwork(networkData, placements);

					virtual_link += TemplateUtil.generateVnfVL(nw_int_name);

				} else {
					// no implementation model for predefine
				}
			}


			String mainYamlContent = TemplateUtil.generateYaml(input, node_template, virtual_link, implementation_model, placements.toString());

			// make zip
			File zipFile = File.createTempFile("ZIP", "1");
			session.setAttribute("mainYamlFileName", mainYamlFileName);
			session.setAttribute("file", zipFile.getAbsolutePath());

			ZipUtil.makeZip(zipFile, metadataContent, mainYamlFileName, mainYamlContent);

		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ResponseEntity<Void>(HttpStatus.OK);
	}

	protected abstract String generateImplModelInternalNetwork(Map<String, String> networkData, List<String> placements) throws IOException;

	protected abstract String generateImplModelExternalNetwork(Map<String, String> networkData, List<String> placements) throws IOException;

	protected abstract String generateNuageInput(Map<String, String> nuageObj) throws IOException;

	protected abstract String generateBasicImplementationModel(Map<String, String> nuageObj, List<String> placements) throws IOException;

	protected abstract String generateInputExternalNetwork(Map<String, String> networkData) throws IOException;

	protected abstract String getNetworkAttributeId() throws IOException;
}
