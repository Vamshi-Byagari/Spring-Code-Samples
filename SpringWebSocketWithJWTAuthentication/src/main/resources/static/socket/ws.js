var socket;
var stompClient;
var vToken = !0;
var v1msh9 = 'replace-this-with-client-A-jwt-token';
var m1pro = 'replace-this-with-client-B-jwt-token';


function initWebSocket(useAuthorization){
	let socketEndpoint = 'ws://localhost:8080/ws';
	if(useAuthorization){
		socketEndpoint = `ws://localhost:8080/ws?token=${vToken ? v1msh9 : m1pro}`;
	}
	socket = new WebSocket(socketEndpoint);

    socket.addEventListener('open', function(e){
		console.log(`socket open: ${e}`);
		socket.send('message');
	});

	socket.addEventListener('error', function(e){
		console.log(`socket error: ${e.code}, ${e.reason}`);
	})

	socket.addEventListener('close', function(e){
		console.log(`stocket close: ${e.code}, ${e.reason}`);
	});
}


function initWebSocketWithSockJs(){
	let socketEndpoint = 'http://localhost:8080/ws';
	if(useAuthorization){
		socketEndpoint = `http://localhost:8080/ws?token=${vToken ? v1msh9 : m1pro}`;
	}
	socket = new SockJS(socketEndpoint);

    socket.addEventListener('open', function(e){
		console.log(`socket open: ${e}`);
		socket.send('message');
	});

	socket.addEventListener('error', function(e){
		console.log(`socket error: ${e.code}, ${e.reason}`);
	})

	socket.addEventListener('close', function(e){
		console.log(`stocket close: ${e.code}, ${e.reason}`);
	});	
}


function initStomp(useAuthorization, useHeaders){
	let socketEndpoint = 'ws://localhost:8080/ws';
	if(useAuthorization){
		socketEndpoint = `ws://localhost:8080/ws?token=${vToken ? v1msh9 : m1pro}`;
	}

	stompClient = Stomp.client(socketEndpoint);
	stompClient.debug = null;

	let headers = {}
	if(useHeaders){
		headers = {
			Authorization:`${vToken ? v1msh9 : m1pro}`
		}
	}

	stompClient.connect(headers, function (frame) {
	    console.log('Success Handler: ' + frame.command);
		stompClient.subscribe('/topic/updates', function(frame){
			console.log('topic-updates :: Subscribe Handler: ' + frame);
		});
		stompClient.subscribe('/user/queue', function(frame){
			console.log('user-queue :: Subscribe Handler: ' + frame);
		});
	}, function(frame){
		console.log(`Error Handler command: ${frame.command} Body: ${frame.body}`);
	});
}


function initStompWithSockJs(useAuthorization, useHeaders){
	let socketEndpoint = 'http://localhost:8080/ws';
	if(useAuthorization){
		socketEndpoint = `http://localhost:8080/ws?${vToken ? v1msh9 : m1pro}`;
	}
	socket = new SockJS(socketEndpoint);

	stompClient = Stomp.over(socket);
	stompClient.debug = null;

	let headers = {}
	if(useHeaders){
		headers = {
			Authorization:`${vToken ? v1msh9 : m1pro}`
		}
	}

	stompClient.connect(headers, function (frame) {
	    console.log('Success Handler: ' + frame.command);
	    isConnected = true;
	    
		stompClient.subscribe('/topic/updates', function(frame){
			console.log('topic-updates :: Subscribe Handler: ' + frame);
		});
		stompClient.subscribe('/user/queue', function(frame){
			console.log('user-queue :: Subscribe Handler: ' + frame);
		});
	}, function(frame){
		console.log('Error Handler: ' + frame.command);
	});
}



(function connect() {

	//initWebSocket(!1);
	//initWebSocketWithSockJs(!1, !1);
	initStomp(!0, !1);
	//initStompWithSockJs(!1, !1);

})();