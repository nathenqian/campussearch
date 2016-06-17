<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
request.setCharacterEncoding("utf-8");
response.setCharacterEncoding("utf-8");
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
		<title>Campus Searchs </title>
		<link href="<%=basePath%>/static/semantic.min.css" rel="stylesheet" type="text/css"/>
	    <link href="<%=basePath%>/static/components/input.min.css" rel="stylesheet" type="text/css"/>
	
	    <script src="<%=basePath%>/static/jquery.js"></script>
	    <script src="<%=basePath%>/static/semantic.js"></script>
	</head>

	<body>
		<form id="form1" name="form1" method="get" action="servlet/ImageServer">
			<input name="query" id="form1_query" type="text" hidden/>
		</form>

		<h1 class="ui center aligned icon purple header" style="font-size: 3em">
			<i class="circular search icon"></i>
			校园搜索引擎
		</h1>
	
		<div class ="ui eleven column grid segment" style="margin: 2em auto">
			<div class="six wide column"></div>

		    <div class="six wide column">
		        <div class="ui icon massive input" style="width: 24em">
		            <input type="text" id = "search_input" placeholder="Type something..." />
		            <i id="search" class="circular search link icon"></i>
		        </div>
		    </div>
		</div>

		<script>
			function search() {
		        $("#form1_query").val($("#search_input").val());
		        $("#form1").submit();
			}

		    $('#search').click(search);
			
			$('#search_input').keypress(function(e) {
				if(e.which == 13) {
					search();
				}
			})
		</script>
	</body>
</html>
