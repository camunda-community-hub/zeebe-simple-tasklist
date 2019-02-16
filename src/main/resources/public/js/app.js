
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
					
						var stompClient = null;
						var notifications = 0;
             
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
								notifications = notifications + 1;
								
								showInfo(notifications + ' ' + message);
						}
	
// --------------------------------------------------------------------

function completeTask(data) {

	var taskKey = document.getElementById("task-key").value;

	$.ajax({
       type : 'PUT',
       url: '/api/tasks/' + taskKey + '/complete',
       data:  JSON.stringify(data),
       contentType: 'application/json; charset=utf-8',
       success: function (result) {
       	showSuccess("Task '" + taskKey + "' completed.");	
       },
       error: function (xhr, ajaxOptions, thrownError) {
      	 showErrorResonse(xhr, ajaxOptions, thrownError);
       },
    	 timeout: 5000,
       crossDomain: true,
    });
}	
