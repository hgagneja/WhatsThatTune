$(function(){
        $("#play").click(function(){
            $.ajax({
             type: "GET",
             url: '/result',
              success:function(response) {
              location.reload(true);
              //do something with 'response'
          }
      });
});
