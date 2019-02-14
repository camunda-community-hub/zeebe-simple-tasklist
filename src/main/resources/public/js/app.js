
function showError(message) {
	document.getElementById("errorText").innerHTML = message;
	$('#errorPanel').show();
}

function showSuccess(message) {
	document.getElementById("successText").innerHTML = message;
	$('#successPanel').show();
}

function showInfo(message) {
	document.getElementById("infoText").innerHTML = message;
	$('#infoPanel').show();
}

function showErrorResonse(xhr, ajaxOptions, thrownError) {
	if (xhr.responseJSON) {
		showError(xhr.responseJSON.message);
	}
	else {
		showError(thrownError);
	}	
}

// --------------------------------------------------------------------

function reload() {
	history.go(0)
}

// --------------------------------------------------------------------
	
function completeTask(taskKey) {

	var formInputs = $(".task-form");
	
	var data = [];
	for (i = 0; i < formInputs.length; i++) {
		var formInput = formInputs[i];
		var key = formInput.id.substr("form_".length);
		var value = formInput.value;
		
		if (formInput.type == "number") {
			value = Number(value)
		} else if (formInput.type == "checkbox") {
			value = Boolean(formInput.checked)
		}
		
		data[i] = { key: key, value: value };
	}

	$.ajax({
       type : 'PUT',
       url: '/api/tasks/' + taskKey + '/complete',
       data:  JSON.stringify(data),
       contentType: 'application/json; charset=utf-8',
       success: function (result) {
       	showSuccess("Task completed.");	
       },
       error: function (xhr, ajaxOptions, thrownError) {
      	 showErrorResonse(xhr, ajaxOptions, thrownError);
       },
    	 timeout: 5000,
       crossDomain: true,
    });
}	
