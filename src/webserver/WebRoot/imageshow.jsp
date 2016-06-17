<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
request.setCharacterEncoding("utf-8");
response.setCharacterEncoding("utf-8");
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path;
String imagePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+"/image/";
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <title>Campus Search</title>

    <link href="<%=basePath%>/static/semantic.min.css" rel="stylesheet" type="text/css"/>
    <link href="<%=basePath%>/static/components/input.min.css" rel="stylesheet" type="text/css"/>

    <script src="<%=basePath%>/static/jquery.js"></script>
    <script src="<%=basePath%>/static/semantic.js"></script>

</head>

<body>
<form id="form1" name="form1" method="get" action="ImageServer">
  <input name="query" id="form1_query" type="text" hidden/>
  <input name="page" id="form1_page" type="text" hidden/>
</form>

<%
    String currentQuery=(String) request.getAttribute("currentQuery");
    int currentPage=(Integer) request.getAttribute("currentPage");
    int maxPage=(Integer) request.getAttribute("maxPage");
%>
<div class ="ui ten column grid segment" style="margin: 1px auto">
    <div class="one wide column">
        <img class="ui tiny image" src="<%=basePath%>/static/tsinghua.png">
    </div>

    <div class="nine wide column">
        <div class="ui icon massive input" style="width: 24em">
            <input type="text" id = "search_input" placeholder="Search..." value="<%=currentQuery%>" />
            <i id="search" class="circular search link icon"></i>
        </div>
    </div>
</div>


<%
    String[] imgcontents=(String[]) request.getAttribute("contents");
    String[] imgtitles=(String[]) request.getAttribute("titles");
    String[] hrefs=(String[]) request.getAttribute("hrefs");
    String[] imgs=(String[]) request.getAttribute("imgs");
    String[] cutstr=(String[]) request.getAttribute("cutstr");
    if(!(imgtitles!=null && imgtitles.length>0))  { %>
    查询不到相应的结果
<% } else { %>

<div class="ui divider"></div>
<div class = "ui grid maingrid" style = "margin : 3px 3px 0;">
    <div class="ui items seven wide column" >
        <% 
            for(int i=0;i<imgtitles.length;i++){
        %>
            <div class="item">
                <div class="ui small image">
                <% if (!imgs[i].equals("")) { %>
					<img src="<%=imgs[i]%>">  </img>
                <%}%>
                </div>
                <div class="content">
                    <div class="header"><a href="<%=hrefs[i]%>"><%=imgtitles[i] %></a></div>
                    <div> <font color="green"> <%=cutstr[i]%> </font> </div>
                    <div class="description"><%=imgcontents[i]%></div>
                    <div class="extra"><button class="ui preview basic button tiny" href-value="<%=hrefs[i]%>"><i class="icon search"></i>预览</button></div>
                </div>
            </div>
        <%} %>
        <div class = "ui segment pagination">
            <%
            if ((currentPage - 1) >= 5) {
            %>
                <div class="btnClass ui tiny button" data-value="1"> << </div>
            <%
            }
            for (int i = 1; i <= maxPage; i++) {
                if (i == currentPage) {
            %>
                    <div class="btnClass ui tiny green button" data-value="<%=i%>">  <%=i%>  </div>
            <%
                } else if ((i - currentPage) < 5 && i - currentPage > -5) {
            %>
                    <div class="btnClass ui tiny button" data-value="<%=i%>"> <%=i%>  </div> 
            <%
                }
            }
            if (maxPage - currentPage >= 5) {
            %>
                <div class="btnClass ui tiny button" data-value="<%=maxPage%>"> >> </div>
            <%
            }
            %>
        </div>
        
    </div>
    <div class="ui items nine wide column" >
        <iframe src="" frameborder="0" height="1000px" width="100%" id = "iframe">   
        </iframe> 
    </div>
</div>

<% } %>
<script>
	function search(x) {
        $("#form1_query").val($("#search_input").val());
        $("#form1_page").val(x);
        $("#form1").submit();
	}

	$(document).ready(function () {
		$('#search').click(function() {
			search(1);
		});

		$('#search_input').keypress(function(e) {
			if(e.which == 13) {
				search(1);
			}
		})
    	
    	$(".btnClass").click(function() {
			search($(this).attr("data-value"))
    	});

    	$(".preview.button").click(function() {
    	    $("#iframe").attr("src", $(this).attr("href-value"));
    	});

		$("#iframe").attr("height", $(".maingrid").css("height"));
	})
</script>

</body>
