/**
 * forget.js
 */
$("button#submit").click(function(){
	doForget();
});
$(document).keydown(function(e) {
	if (e.keyCode == 13) {
		doForget();
	}
});

function doForget(){
	var email = $("#forgetForm #email").val();
	if(email=="")
		return;
	
	$.ajax( {    
	    url:'/home/users/forgetpassword',// 跳转到 action    
	    data:$('#forgetForm').serialize(),    
	    type:'POST',
	    processData:true,
	    contentType:"application/x-www-form-urlencoded",
	    success:function(data) {   
	    	alert(data.status);
	    	if(data.status=="0"){
	    		location.href = "/home/users/login";
	    	}else if(data.status=="-1"){
	    		alert(data.data.errMessage);
	    	}
	     },    
	     error : function(e) {    
	          // view("异常！");    
	          alert(e);    
	     }    
	}); 
	
}