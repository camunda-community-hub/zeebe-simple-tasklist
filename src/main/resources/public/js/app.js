
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


function withSecurityToken(url) {

	var csrf = document.getElementById("_csrf").value;

	return url + '?_csrf=' + csrf
}

// --------------------------------------------------------------------
					
var stompClient = null;
             
            function connect() {
                var socket = new SockJS('/notifications');
                stompClient = Stomp.over(socket);  
                stompClient.connect({}, function(frame) {
                    stompClient.subscribe('/notifications/tasks', function(message) {
                      handleMessage(JSON.parse(message.body));
                    });
                });
            }
             
            function disconnect() {
                if(stompClient != null) {
                    stompClient.disconnect();
                }
            }
             
            function sendMessage(msg) {
                stompClient.send("/notifications", {}, 
                  JSON.stringify(msg));
            }
             
            function handleMessage(msg) {
                var message = msg.message;

                showInfo(message);
            }
	
// --------------------------------------------------------------------

function completeTask(data) {

	var taskKey = document.getElementById("task-key").value;

	$.ajax({
       type : 'PUT',
       url: withSecurityToken('/api/tasks/' + taskKey + '/complete'),
       data:  JSON.stringify(data),
       contentType: 'application/json; charset=utf-8',
       success: function (result) {
       	showSuccess("Task completed. (Key: " + taskKey + ")");	
       },
       error: function (xhr, ajaxOptions, thrownError) {
      	 showErrorResonse(xhr, ajaxOptions, thrownError);
       },
    	 timeout: 5000,
       crossDomain: true,
    });
}

// --------------------------------------------------------------------

function claimTask(taskKey) {

	$.ajax({
       type : 'PUT',
       url: withSecurityToken('/api/tasks/' + taskKey + '/claim'),
       contentType: 'application/json; charset=utf-8',
       success: function (result) {
       	showSuccess("Task claimed. (Key: " + taskKey + ")");	
       },
       error: function (xhr, ajaxOptions, thrownError) {
      	 showErrorResonse(xhr, ajaxOptions, thrownError);
       },
    	 timeout: 5000,
       crossDomain: true,
    });
}

// --------------------------------------------------------------------

function createUser() {

	var username = document.getElementById("user-name").value;
	var password = document.getElementById("user-password").value;

	var data = {
		username: username,
		password: password
	};

	$.ajax({
       type : 'POST',
       url: withSecurityToken('/api/users/'),
       data:  JSON.stringify(data),
       contentType: 'application/json; charset=utf-8',
       success: function (result) {
       	showSuccess("User created. (Username: " + username + ")");	
       },
       error: function (xhr, ajaxOptions, thrownError) {
      	 showErrorResonse(xhr, ajaxOptions, thrownError);
       },
    	 timeout: 5000,
       crossDomain: true,
    });
}

// --------------------------------------------------------------------

function deleteUser(username) {

	$.ajax({
       type : 'DELETE',
       url: withSecurityToken('/api/users/' + username),
       contentType: 'application/json; charset=utf-8',
       success: function (result) {
       	showSuccess("User deleted. (Username: " + username + ")");	
       },
       error: function (xhr, ajaxOptions, thrownError) {
      	 showErrorResonse(xhr, ajaxOptions, thrownError);
       },
    	 timeout: 5000,
       crossDomain: true,
    });
}

// --------------------------------------------------------------------

function updateGroupMemberships(username) {

	var elements = $(".group-member-" + username);
	
	var groups = [];
	
	for (var i = 0; i < elements.length; i++) {
		var element = elements[i];
		if (element.checked) {
			groups.push(element.value);
		}
  }
	
	$.ajax({
       type : 'PUT',
       url: withSecurityToken('/api/users/' + username + '/group-memberships'),
       data: JSON.stringify(groups),
       contentType: 'application/json; charset=utf-8',
       success: function (result) {
       	showSuccess("User updated. (Username: " + username + ")");	
       },
       error: function (xhr, ajaxOptions, thrownError) {
      	 showErrorResonse(xhr, ajaxOptions, thrownError);
       },
    	 timeout: 5000,
       crossDomain: true,
    });
}

// --------------------------------------------------------------------

function createGroup() {

	var name = document.getElementById("group-name").value;

	$.ajax({
       type : 'POST',
       url: withSecurityToken('/api/groups/'),
       data:  name,
       contentType: 'application/json; charset=utf-8',
       success: function (result) {
       	showSuccess("Group created. (Name: " + name + ")");	
       },
       error: function (xhr, ajaxOptions, thrownError) {
      	 showErrorResonse(xhr, ajaxOptions, thrownError);
       },
    	 timeout: 5000,
       crossDomain: true,
    });
}

// --------------------------------------------------------------------

function deleteGroup(name) {

	$.ajax({
       type : 'DELETE',
       url: withSecurityToken('/api/groups/' + name),
       contentType: 'application/json; charset=utf-8',
       success: function (result) {
       	showSuccess("Group deleted. (Name: " + name + ")");	
       },
       error: function (xhr, ajaxOptions, thrownError) {
      	 showErrorResonse(xhr, ajaxOptions, thrownError);
       },
    	 timeout: 5000,
       crossDomain: true,
    });
}
