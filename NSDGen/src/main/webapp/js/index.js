function guid() {
	return s4() + s4() + '-' + s4() + '-' + s4() + '-' + s4() + '-' + s4() + s4() + s4();
}

function s4() {
	return Math.floor((1 + Math.random()) * 0x10000).toString(16).substring(1);
}

var network_changed = false;
var vnf_changed = false;

function downloadIt(type) {
	var formObj = $('#formMetadata');
	var metadata = $(formObj).serializeArray();

	console.log("metadata := " + JSON.stringify(metadata));

	var formObj = $('#formNetworkTable');
	var networks = $(formObj).serializeArray();

	console.log("networks := " + JSON.stringify(networks));
	
	var formObj = $('#formVnfs');
	var vnf = $(formObj).serializeArray();
	
	console.log("vnf := " + JSON.stringify(vnf));
	
	var nuage = undefined;
	if ('nuage' == type) {
		var formObj = $('#formNuage');
		nuage = $(formObj).serializeArray();
	}

//	if ($('div#vnfm_huawei').hasClass('active')) {
//		var formObj = $('#formVnfmHuawei');
//		vnf = $(formObj).serializeArray();
//		/*
//		 * vnf.push({ name : "vnf_type", value : "huawei" }); console.log("vnf := " +
//		 * JSON.stringify(vnf));
//		 */
//
//	} else if ($('div#vnfm_zte').hasClass('active')) {
//		var formObj = $('#formVnfmZte');
//		vnf = $(formObj).serializeArray();
//		/*
//		 * vnf.push({ name : "vnf_type", value : "zte" });
//		 */
//	} else {
//		var formObj = $('#formVnfmCbam');
//		vnf = $(formObj).serializeArray();
//		/*
//		 * vnf.push({ name : "vnf_type", value : "cbam" });
//		 */
//	}

	$.ajax({
		type : "POST",
		contentType : 'application/json',
		url : '/' + type + '/generate',
		data : JSON.stringify({
			"metadata" : JSON.stringify(metadata),
			"networks" : JSON.stringify(networks),
			"vnf" : JSON.stringify(vnf),
			"nuage" : JSON.stringify(nuage)
		}),
		success : function(retData) {
			$("body").append("<iframe src='/" + type + "/download' style='display: none;' ></iframe>");
		}
	});
}

function validateRequired(input) {
	var valid = true;
	input.each(function() {
		var inputObj = $(this);
		var formGroupObj = inputObj.parent('div');
		formGroupObj.removeClass('has-danger');
		inputObj.removeClass('form-control-danger');
		inputObj.parent().find('.form-control-feedback').remove();
		if (null == inputObj.val() || "" == inputObj.val()) {
			formGroupObj.addClass('has-danger');
			inputObj.addClass('form-control-danger');
			inputObj.parent().append('<div class="form-control-feedback">This value is required</div>');
			valid = false;
		}
	});

	return valid;
}

function saveNetwork(formObj) {
	var data = $(formObj).serializeArray();
	
	var type = data.get('nw_type');
	var nw_name = "";
	var cidr = "";
	if (type == 'external') {
		nw_name = data.get('nw_ext_name');
		cidr = data.get('nw_ext_cidr');
		if (!validateRequired($('#nw_ext_name'))) {
			return false;
		}
	} else if (type == 'internal') {
		nw_name = data.get('nw_int_name');
		cidr = data.get('nw_int_cidr');
		if (!validateRequired($('#nw_int_name'))) {
			return false;
		}
	} else {
		nw_name = data.get('nw_p_name');
		if (!validateRequired($('#nw_p_name'))) {
			return false;
		}
	}

	var html_tr = "<tr><td><input type='hidden' name='network_data' value='" + JSON.stringify(data) + "'>" + type + "</td><td>" + nw_name + "</td><td>" + cidr + "</td>";
	html_tr += "<td class='d-flex justify-content-end'><button type='button' class='btn btn-small btn-danger' onclick='network_changed=true;$(this).closest(\"tr\").remove()'>Delete</button></td></tr>";
	formObj.trigger("reset");
	$('#networkTable').append(html_tr);

	network_changed = true;
}

function addConnectionPoint(tableObj, uuid) {
	if (uuid == undefined) {
		uuid = guid();
	}
	var html_select = "<div class='input-group-addon'>-></div>";
	var select_name = "connection_point_mapping_" + uuid;
	var input_name = "connection_point_name_" + uuid;
	var connection_point_mapping_select = "<select class='form-control' name='" + select_name + "' id='" + select_name +"'>";
	connection_point_mapping_select += connection_point_mapping_option;
	connection_point_mapping_select += "</select>";
	html_select += connection_point_mapping_select;

	var html_tr = "<tr><td><div class='input-group'><input type='text' name='" + input_name + "' id='" + input_name + "' class='form-control' placeholder='name'>" + html_select + "</dev></td>";
	html_tr += "<td class='d-flex justify-content-end'><button type='button' class='btn btn-small btn-danger' onclick='$(this).closest(\"tr\").remove()'>-</button></td></tr>";
	tableObj.append(html_tr);

	return false;
}

function notifyVnfChanged() {
	var vnfs = [ '' ];
	$("input[name='vnf']").each(function() { // first pass, create name
												// mapping
		var vnf = JSON.parse($(this).val());
		var type = vnf.get('vnf_type');
		if (type == 'huawei') {
			vnfs.push(vnf.get('vnfm_h_name'));
		} else if (type == 'zte') {
			vnfs.push(vnf.get('vnfm_z_name'));
		} else {
			vnfs.push(vnf.get('vnfm_cbam_name'));
		}
	});

	var vnf_depends_options = "";

	$.each(vnfs, function(index, value) {
		vnf_depends_options += "<option value='" + value + "'>" + value + "</option>";
	});

	$("select[id='vnf_depend']").each(function() {
		$(this).empty();
		$(this).append(vnf_depends_options);
	});

	$("select[id='vnf_depend']").change(function() {
		var selectObj = $(this);
		var selectVal = selectObj.val();
		// var vnf = $(this).closest("input[name='vnf']");

		$(this).closest('tr').find("input[name='vnf']").each(function() {
			var vnf = JSON.parse(this.value);
			var type = vnf.get('vnf_type');
			if (type == 'huawei') {
				name = vnf.get('vnfm_h_name');
			} else if (type == 'zte') {
				name = vnf.get('vnfm_z_name');
			} else {
				name = vnf.get('vnfm_cbam_name');
			}

			if (name == selectVal) {
				selectObj.val('');
			} else {
				for (var i = 0; i < vnf.length; ++i) {
					if (vnf[i]['name'] === 'vnf_depend') {
						vnf[i]['value'] = selectVal;
					}
				}
			}

			this.value = JSON.stringify(vnf);
		});

	});

}

function saveVnf(formObj) {
	var data = $(formObj).serializeArray();

	var vnf_uuid = formObj.find('input[name="vnf_uuid"]').val(); 
	
	var type = data.get('vnf_type');
	var name = undefined;
	var valid = true;
	if (type == 'huawei') {
		if (!validateRequired($('#vnfm_h_name'))) {
			valid = false;
		}
		name = data.get('vnfm_h_name');
	} else if (type == 'zte') {
		if (!validateRequired($('#vnfm_z_name'))) {
			valid = false;
		}
		name = data.get('vnfm_z_name');
	} else {
		if (!validateRequired($('#vnfm_cbam_name'))) {
			valid = false;
		}
		name = data.get('vnfm_cbam_name');
	}

	if (!validateRequired($('input[name*="connection_point_name"]')) || !validateRequired($('select[name*="connection_point_mapping"]'))) {
		valid = false;
	}

	if (!valid) {
		return false;
	}
	resetVnf(formObj);
	
	if ("" != vnf_uuid) {
		//edit
		$('#' + vnf_uuid).val(JSON.stringify(data));
	} else {
		//add
		var vnf_select = "<select class='form-control' id='vnf_depend'>";
		vnf_select += "</select>";
		
		var uuid = guid();

		var html_tr = "<tr><td><input type='hidden' name='vnf' id='vnf_" + uuid + "' value='" + JSON.stringify(data) + "'>" + "<a href='#' onclick='viewVnf(\"vnf_" + uuid + "\")'>" + name + "</a>" + "</td><td>" + vnf_select + "</td>";
		html_tr += "<td class='d-flex justify-content-end'><button type='button' class='btn btn-small btn-danger' onclick='$(this).closest(\"tr\").remove();notifyVnfChanged()'>Delete</button></td></tr>";
		$('#vnfTable').append(html_tr);
		notifyVnfChanged();
	}
}

function resetVnf(formObj) {
	formObj.trigger("reset");
	formObj.find("table tbody tr").each(function() {
		$(this).remove();
	});
	$('#vnfm_h_name').removeAttr('readonly');
}

function viewVnf(uuid) {
	var vnf = JSON.parse($('#' + uuid).val());
	
	$('input[name="vnf_uuid"]').val(uuid);
	
	
	
	var type = undefined;
	for (var i = 0; i < vnf.length; ++i) {
		if (vnf[i]['name'] === 'vnf_type') {
			type = vnf[i]['value'];
			break;
		}
	}
	
	if (type == 'huawei') {
		$('#vnf a[href="#vnfm_huawei"]').tab('show');
		resetVnf($('#formVnfmHuawei'));
		$('#vnfm_h_name').attr('readonly', 'readonly');
		for (var i = 0; i < vnf.length; ++i) {
			var name = vnf[i]['name'];
			var value = vnf[i]['value'];
			
			if (name.startsWith("connection_point_name_")) {
				var uuid = name.split('connection_point_name_')[1];
				addConnectionPoint($('#tableVnfmHuawei'), uuid);
				
				$('#connection_point_name_' + uuid).val(value);
				
				for (var j = 0; j < vnf.length; ++j) {
					if (vnf[j]['name'] == 'connection_point_mapping_' + uuid) {
						$('#connection_point_mapping_' + uuid).val(vnf[j]['value']);
					}
				}
			} else {
				$('#' + name).val(value);
			}
		}
		
	} else if (type == 'zte') {
		$('#vnf a[href="#vnfm_zte"]').tab('show');
		resetVnf($('#formVnfmZte'));
		$('#vnfm_z_name').attr('readonly', 'readonly');
		for (var i = 0; i < vnf.length; ++i) {
			var name = vnf[i]['name'];
			var value = vnf[i]['value'];
			
			if (name.startsWith("connection_point_name_")) {
				var uuid = name.split('connection_point_name_')[1];
				addConnectionPoint($('#tableVnfmZte'), uuid);
				
				$('#connection_point_name_' + uuid).val(value);
				
				for (var j = 0; j < vnf.length; ++j) {
					if (vnf[j]['name'] == 'connection_point_mapping_' + uuid) {
						$('#connection_point_mapping_' + uuid).val(vnf[j]['value']);
					}
				}
			} else {
				$('#' + name).val(value);
			}
		}
	} else {
		$('#vnf a[href="#vnfm_cbam"]').tab('show');
	}
	
}

function gotoVNF() {
	$('#allTabs a[href="#vnf"]').removeClass('disabled');
	$('#allTabs a[href="#vnf"]').tab('show');
}

function gotoNetwork() {
	$('#allTabs a[href="#networks"]').removeClass('disabled');
	$('#allTabs a[href="#networks"]').tab('show');
}

var connection_point_mapping_option = "";

$(document).ready(function() {

	Array.prototype.get = function(name) {
		for (var i = 0, len = this.length; i < len; i++) {
			if (typeof this[i] != "object")
				continue;
			if (this[i].name === name)
				return this[i].value;
		}
	};

	$('#allTabs a[href="#vnf"]').on('show.bs.tab', function(e) {
		var formObj = $('#formNetworkTable');
		var networks = $(formObj).serializeArray();
		connection_point_mapping_option = "";

		for (i = 0; i < networks.length; i++) {
			var network = JSON.parse(networks[i]['value']);
			var type = network.get('nw_type');
			if (type == 'external') {
				nw_name = network.get('nw_ext_name');
				cidr = network.get('nw_ext_cidr');
			} else if (type == 'internal') {
				nw_name = network.get('nw_int_name');
				cidr = network.get('nw_int_cidr');
			} else {
				nw_name = network.get('nw_p_name');
			}
			connection_point_mapping_option += "<option value='" + nw_name + "'>" + nw_name + "</option>";
		}

		if (network_changed) {
			network_changed = false;
			$("select[name*='connection_point_mapping']").each(function() { // first
																			// pass,
																			// create
																			// name
																			// mapping
				$(this).empty();
				$(this).append(connection_point_mapping_option);
			});
		}
	});

	$('#allTabs a[href="#nsd"]').on('show.bs.tab', function(e) {
		var valid = true;

		var formObj = $('#formMetadata');
		var metadata = $(formObj).serializeArray();
		if (!validateRequired($('input[name="meta_name"]'))) {
			valid = false;
		}
		if (!validateRequired($('input[name="meta_version"]'))) {
			valid = false;
		}
		if (!valid) {
			$('#allTabs a[href="#metadata"]').tab('show');
			return false;
		}

		if (!valid) {
			$('#allTabs a[href="#vnf"]').tab('show');
			return false;
		}
	});
});