$(document).ready(function(){
	$('#q').on('input', function(e){
			var words = e.target.value.split(" ");
			var prefix = words.slice(0, -1).join(" ");
			var query = words[words.length - 1];
			if(query != ''){
				$.ajax({
					url: "auto_data.php?q=" + query,
					success: function(result){
						var jsonarr = JSON.parse(result);
						$("#search-suggest").empty();
						for(var i=0; i< jsonarr.length; i++){
							$("#search-suggest").append("<a href='#'>" + prefix + " " + jsonarr[i] + "</a>");
						}
						$("#search-suggest").css("display", "block");
					},

					error: function(result){

					}
				});
			}
			else{
				$("#search-suggest").css("display", "none");
			}
		}
	);

	$('#search-suggest').on('click', 'a', function(event){
		event.preventDefault();
		$('#q').val(event.target.text);
	});
	//
	// $('#q').keypress(function(event){
	//   if(event.which == 13){
	//     var spellresult = spellCheck();
	//     if(spellresult !== true){
	//       $('#searchresult').html("");
	//       $("#spell-suggestion-word").attr('href', '/572hw4/searchui.php?q=' + spellresult);
	//       $("#spell-suggestion-word").text(spellresult);
	//       $('#spell-suggestion').css("display", "block");
	//     }
	//     else{
	//       $('#search').submit();
	//     }
	//   }
	// });
	//
	// $('#submit').click(function(event){
	//   var spellresult = spellCheck();
	//   if(spellresult !== true){
	//     event.preventDefault();
	//     $('#searchresult').html("");
	//     $("#spell-suggestion-word").attr('href', '/572hw4/searchui.php?q=' + spellresult);
	//     $("#spell-suggestion-word").text(spellresult);
	//     $('#spell-suggestion').css("display", "block");
	//   }
	// });

	$(window).click(function(){
		$("#search-suggest").css("display", "none");
	});


});
