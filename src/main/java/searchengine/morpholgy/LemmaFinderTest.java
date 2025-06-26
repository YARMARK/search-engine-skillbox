package searchengine.morpholgy;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class LemmaFinderTest {

    private static String text = "<!doctype html>\n" +
                                 "<html>\n" +
                                 " <head>\n" +
                                 "  <title>Купить планшеты</title>\n" +
                                 "  <meta name=\"description\" content=\"купить планшеты, купить самовывозом планшеты\">\n" +
                                 "  <meta name=\"keywords\" content=\"Планшеты, купитьпланшеты\">\n" +
                                 "  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n" +
                                 "  <meta http-equiv=\"Last-Modified\" content=\"Mon, 14 Feb 2022 22:01:52 GMT\">\n" +
                                 "  <link rel=\"shortcut icon\" href=\"/favicon.ico\">\n" +
                                 "  <link rel=\"apple-touch-icon\" href=\"/logo_apple.png\">\n" +
                                 "  <link rel=\"StyleSheet\" href=\"/include_new/styles.css\" type=\"text/css\" media=\"all\">\n" +
                                 "  <link rel=\"stylesheet\" href=\"/include_new/jquery-ui.css\">\n" +
                                 "  <script src=\"https://code.jquery.com/jquery-1.8.3.js\"></script>\n" +
                                 "  <script src=\"https://code.jquery.com/ui/1.10.0/jquery-ui.js\"></script>\n" +
                                 "  <script src=\"/jscripts/jquery.inputmask.js\" type=\"text/javascript\"></script>\n" +
                                 "  <script src=\"/jscripts/jquery.inputmask.extensions.js\" type=\"text/javascript\"></script>\n" +
                                 "  <script src=\"/jscripts/jquery.inputmask.numeric.extensions.js\" type=\"text/javascript\"></script>\n" +
                                 "  <link rel=\"stylesheet\" type=\"text/css\" href=\"/fancybox/jquery.fancybox-1.3.4.css\" media=\"screen\">\n" +
                                 "  <script type=\"text/javascript\" src=\"/fancybox/jquery.mousewheel-3.0.4.pack.js\"></script>\n" +
                                 "  <script type=\"text/javascript\" src=\"/fancybox/jquery.fancybox-1.3.4.js\"></script>\n" +
                                 "  <script type=\"text/javascript\" src=\"/include_new/playback.js\"></script>\n" +
                                 "  <script>\n" +
                                 "  $( function() {\n" +
                                 "    $( \"#accordion\" ).accordion({\n" +
                                 "      heightStyle: \"content\",\n" +
                                 "\t  collapsible: true,\n" +
                                 "\t  active : false,\n" +
                                 "\t  activate: function( event, ui ) {\n" +
                                 "         if ($(ui.newHeader).offset() != null) {\n" +
                                 "        ui.newHeader,\n" +
                                 "        $(\"html, body\").animate({scrollTop: ($(ui.newHeader).offset().top)}, 500);\n" +
                                 "      }\n" +
                                 "    }\n" +
                                 "    });\n" +
                                 "\t} );\n" +
                                 "\t$( function() {\n" +
                                 "    var icons = {\n" +
                                 "      header: \"ui-icon-circle-arrow-e\",\n" +
                                 "      activeHeader: \"ui-icon-circle-arrow-s\"\n" +
                                 "    };\n" +
                                 "    $( \"#accordion\" ).accordion({\n" +
                                 "      icons: icons\n" +
                                 "    });\n" +
                                 "    $( \"#toggle\" ).button().on( \"click\", function() {\n" +
                                 "      if ( $( \"#accordion\" ).accordion( \"option\", \"icons\" ) ) {\n" +
                                 "        $( \"#accordion\" ).accordion( \"option\", \"icons\", null );\n" +
                                 "      } else {\n" +
                                 "        $( \"#accordion\" ).accordion( \"option\", \"icons\", icons );\n" +
                                 "      }\n" +
                                 "    });\n" +
                                 "  } );\n" +
                                 "  </script>\n" +
                                 "  <script type=\"text/javascript\">\n" +
                                 "  $(function() {\n" +
                                 " \n" +
                                 "$(window).scroll(function() {\n" +
                                 " \n" +
                                 "if($(this).scrollTop() != 0) {\n" +
                                 " \n" +
                                 "$('#toTop').fadeIn();\n" +
                                 " \n" +
                                 "} else {\n" +
                                 " \n" +
                                 "$('#toTop').fadeOut();\n" +
                                 " \n" +
                                 "}\n" +
                                 " \n" +
                                 "});\n" +
                                 " \n" +
                                 "$('#toTop').click(function() {\n" +
                                 " \n" +
                                 "$('body,html').animate({scrollTop:0},800);\n" +
                                 " \n" +
                                 "});\n" +
                                 " \n" +
                                 "});\n" +
                                 " \n" +
                                 "</script>\n" +
                                 " </head>\n" +
                                 " <body class=\"body_undertop\" topmargin=\"0\" leftmargin=\"0\" bottommargin=\"0\" rightmargin=\"0\" align=\"center\">\n" +
                                 "  <table class=\"table1\" style=\"box-shadow:0px 0px 32px #595959; margin:5px auto; \" bgcolor=\"#ffffff\" width=\"1024\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\">\n" +
                                 "   <tbody>\n" +
                                 "    <tr>\n" +
                                 "     <td colspan=\"3\" width=\"1024\">\n" +
                                 "      <table width=\"100%\" border=\"0\" height=\"110px\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin-top: 0px; margin-bottom: 0px;\">\n" +
                                 "       <tbody>\n" +
                                 "        <tr>\n" +
                                 "         <td width=\"365px\" rowspan=\"2\" align=\"left\">\n" +
                                 "          <table width=\"250px\" align=\"left\">\n" +
                                 "           <tbody>\n" +
                                 "            <tr>\n" +
                                 "             <td width=\"60px\" height=\"60px\"><img onclick=\"document.location='http://www.playback.ru';return false\" src=\"/img_new/lolo.png\" class=\"logotip\" alt=\"Playback.ru - фотоаппараты, видеокамеры и аксессуары к ним\" title=\"Playback.ru - фотоаппараты, видеокамеры и аксессуары к ним\"></td>\n" +
                                 "             <td valign=\"center\" align=\"left\"><a class=\"tele_span\" href=\"/\"><span class=\"tele_span_playback\">PlayBack.ru</span></a><br><span style=\"cursor: pointer;\" onclick=\"document.location='/waytoplayback.html';return false\" class=\"getcallback2\">5 минут от метро ВДНХ</span></td>\n" +
                                 "            </tr>\n" +
                                 "           </tbody>\n" +
                                 "          </table></td>\n" +
                                 "         <td width=\"3px\" rowspan=\"2\" align=\"center\">&nbsp;</td>\n" +
                                 "         <td width=\"290px\" rowspan=\"2\">\n" +
                                 "          <table width=\"215px\" align=\"center\">\n" +
                                 "           <tbody>\n" +
                                 "            <tr>\n" +
                                 "             <td valign=\"center\" align=\"center\"><span class=\"tele_span\"><nobr><a href=\"tel:+74951437771\">8(495)143-77-71</a></nobr></span><span class=\"grrafik\"><nobr><br>\n" +
                                 "                пн-пт: c 11 до 20<br>\n" +
                                 "                сб-вс: с 11 до 18</nobr></span></td>\n" +
                                 "            </tr>\n" +
                                 "           </tbody>\n" +
                                 "          </table></td>\n" +
                                 "         <td width=\"3px\" align=\"center\" rowspan=\"2\">&nbsp;</td>\n" +
                                 "         <td width=\"185px\">\n" +
                                 "          <table width=\"175px\" align=\"center\">\n" +
                                 "           <tbody>\n" +
                                 "            <tr>\n" +
                                 "             <td valign=\"center\" align=\"center\"><span class=\"blocknamezpom\" style=\"cursor: pointer;\" onclick=\"document.location='/tell_about_the_problem.html';return false\">Возникла проблема?<br>\n" +
                                 "               Напишите нам!</span></td>\n" +
                                 "            </tr>\n" +
                                 "           </tbody>\n" +
                                 "          </table><span class=\"tele_span\"></span></td>\n" +
                                 "         <td width=\"3px\" align=\"center\">&nbsp;</td>\n" +
                                 "         <td width=\"179px\">\n" +
                                 "          <table width=\"175px\" align=\"center\">\n" +
                                 "           <tbody>\n" +
                                 "            <tr>\n" +
                                 "             <td width=\"53px\" height=\"50px\" rowspan=\"2\" align=\"left\"><a href=\"/basket.html\"><img src=\"/img_new/basket.png\" width=\"49px\" border=\"0\"></a></td>\n" +
                                 "             <td valign=\"bottom\" align=\"left\" height=\"25px\"><a class=\"tele_span2\" href=\"/basket.html\">Корзина</a><br><span class=\"take_me_call\"></span></td>\n" +
                                 "            </tr>\n" +
                                 "            <tr>\n" +
                                 "             <td height=\"10px\" align=\"right\" valign=\"top\"><span class=\"basket_inc_label\" id=\"sosotoyaniekorziny\">пуста</span></td>\n" +
                                 "            </tr>\n" +
                                 "           </tbody>\n" +
                                 "          </table></td>\n" +
                                 "        </tr>\n" +
                                 "        <tr>\n" +
                                 "         <td colspan=\"3\" style=\"text-align: right;\">\n" +
                                 "          <form action=\"/search.php\" method=\"get\" class=\"izkat\">\n" +
                                 "           <input type=\"search\" name=\"search_string\" placeholder=\"поиск\" class=\"ssstring\"> <input type=\"submit\" name=\"\" value=\"Искать\" class=\"iskat\">\n" +
                                 "          </form></td>\n" +
                                 "        </tr>\n" +
                                 "       </tbody>\n" +
                                 "      </table></td><!--\t<tr> \n" +
                                 "\t<td colspan=\"3\" style=\"color: #2556A3; font:17px Roboto-Regular,Helvetica,sans-serif; text-align: center; height: 35px;vertical-align: middle;padding-bottom:10px;\">\n" +
                                 "\t\t<b>Уважаемые покупатели! C 28 апреля по 8 мая работаем по обычному графику. 9 мая - выходной.</b>\n" +
                                 "\t</td>\n" +
                                 "  </tr>--->\n" +
                                 "    </tr>\n" +
                                 "    <tr>\n" +
                                 "     <td colspan=\"3\" style=\"text-align: center;\">\n" +
                                 "      <nav>\n" +
                                 "       <ul class=\"topmenu\">\n" +
                                 "        <li><a href=\"\" class=\"active\" onclick=\"return false;\"><img src=\"/img/imglist.png\" height=\"9px\"> Каталог<span class=\"fa fa-angle-down\"></span></a>\n" +
                                 "         <ul class=\"submenu\">\n" +
                                 "          <li><a href=\"/catalog/1652.html\">Чехлы для смартфонов Infinix</a></li>\n" +
                                 "          <li><a href=\"/catalog/1511.html\">Смартфоны</a></li>\n" +
                                 "          <li><a href=\"/catalog/1300.html\">Чехлы для смартфонов Xiaomi</a></li>\n" +
                                 "          <li><a href=\"/catalog/1302.html\">Защитные стекла для смартфонов Xiaomi</a></li>\n" +
                                 "          <li><a href=\"/catalog/1310.html\">Чехлы для Huawei/Honor</a></li>\n" +
                                 "          <li><a href=\"/catalog/1308.html\">Чехлы для смартфонов Samsung</a></li>\n" +
                                 "          <li><a href=\"/catalog/1307.html\">Защитные стекла для смартфонов Samsung</a></li>\n" +
                                 "          <li><a href=\"/catalog/1141.html\">Планшеты</a></li>\n" +
                                 "          <li><a href=\"/catalog/1315.html\">Зарядные устройства и кабели</a></li>\n" +
                                 "          <li><a href=\"/catalog/1329.html\">Держатели для смартфонов</a></li>\n" +
                                 "          <li><a href=\"/catalog/665.html\">Автодержатели</a></li>\n" +
                                 "          <li><a href=\"/catalog/1304.html\">Носимая электроника</a></li>\n" +
                                 "          <li><a href=\"/catalog/1305.html\">Наушники и колонки</a></li>\n" +
                                 "          <li><a href=\"/catalog/805.html\">Запчасти для телефонов</a></li>\n" +
                                 "          <li><a href=\"/catalog/1311.html\">Чехлы для планшетов</a></li>\n" +
                                 "          <li><a href=\"/catalog/1317.html\">Аксессуары для фото-видео</a></li>\n" +
                                 "          <li><a href=\"/catalog/1318.html\">Чехлы для смартфонов Apple</a></li>\n" +
                                 "          <li><a href=\"/catalog/1429.html\">USB Флеш-накопители</a></li>\n" +
                                 "          <li><a href=\"/catalog/1473.html\">Товары для детей</a></li>\n" +
                                 "          <li><a href=\"/catalog/1507.html\">Защитные стекла для смартфонов Realme</a></li>\n" +
                                 "          <li><a href=\"/catalog/1508.html\">Чехлы для смартфонов Realme</a></li>\n" +
                                 "          <li><a href=\"/catalog/18.html\">Карты памяти</a></li>\n" +
                                 "          <li><a href=\"/catalog/1303.html\">Защитные стекла для планшетов</a></li>\n" +
                                 "          <li><a href=\"/catalog/1312.html\">Защитные стекла для смартфонов</a></li>\n" +
                                 "          <li><a href=\"/catalog/1622.html\">Защитные стекла для смартфонов Apple</a></li>\n" +
                                 "          <li><a href=\"/catalog/1626.html\">Чехлы для смартфонов Vivo</a></li>\n" +
                                 "          <li><a href=\"/catalog/1636.html\">Чехлы для смартфонов Tecno</a></li>\n" +
                                 "         </ul></li>\n" +
                                 "        <li><a href=\"/dostavka.html\">Доставка</a></li>\n" +
                                 "        <li><a href=\"/pickup.html\">Самовывоз</a></li>\n" +
                                 "        <li><a href=\"/payment.html\">Оплата</a></li>\n" +
                                 "        <li><a href=\"/warranty.html\">Гарантия и обмен</a></li>\n" +
                                 "        <li><a href=\"/contacts.html\">Контакты</a></li>\n" +
                                 "       </ul>\n" +
                                 "      </nav></td>\n" +
                                 "    </tr>\n" +
                                 "    <tr>\n" +
                                 "     <td colspan=\"3\" valign=\"top\">\n" +
                                 "      <table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n" +
                                 "       <tbody>\n" +
                                 "        <tr>\n" +
                                 "         <!----<td class=\"menu_full_cell\" width=\"253\">---->\n" +
                                 "         <td colspan=\"2\" class=\"item_full_cell\" itemscope itemtype=\"http://schema.org/ItemList\">\n" +
                                 "          <link itemprop=\"url\" href=\"http://www.playback.ru/catalog/1141.html\">\n" +
                                 "          <table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n" +
                                 "           <tbody>\n" +
                                 "            <tr>\n" +
                                 "             <td colspan=\"2\" class=\"full_route2\">\n" +
                                 "              <div style=\"width: 100%; text-align: left; padding-top: 5px;\">\n" +
                                 "               <a class=\"button15\" href=\"/\">◄ Наши спецпредложения</a>\n" +
                                 "              </div></td>\n" +
                                 "            </tr><!--<tr>\n" +
                                 "\t\t\t <td colspan=\"2\" height=\"210px\">\n" +
                                 "\t\t\t <div id=\"featured\"> \n" +
                                 "\t\t\t\t<img src=\"/promotion/dslr_plus_card.jpg\" alt=\"Скидка на  карту при покупке зеркального фотоаппарата\" />\n" +
                                 "\t\t\t\t<img src=\"/promotion/foto_plus_card.jpg\" alt=\"Скидка на чехол карту при покупке компактного фотоаппарата\" />\n" +
                                 "\t\t\t\t<img src=\"/promotion/video_plus_card.jpg\" alt=\"Скидка на  карту при покупке видеокамеры\" />\n" +
                                 "\t\t\t</div>\n" +
                                 "\t\t\t  </td>\n" +
                                 "\t\t\t</tr>--->\n" +
                                 "            <tr>\n" +
                                 "             <td colspan=\"2\" class=\"catalog_name\">\n" +
                                 "              <h1>Планшеты</h1></td>\n" +
                                 "            </tr>\n" +
                                 "            <tr>\n" +
                                 "             <td colspan=\"2\" class=\"reg_content_otb\">Отбор по производителю: <a class=\"ashka\" href=\"/catalog/1141.html/brand/5/sort/0/page/1\">Samsung</a> | <a class=\"ashka\" href=\"/catalog/1141.html/brand/980/sort/0/page/1\">Xiaomi</a> | <a class=\"ashka\" href=\"/catalog/1141.html/brand/0/sort/0/page/1\">Все</a></td>\n" +
                                 "            </tr>\n" +
                                 "            <tr>\n" +
                                 "             <td colspan=\"2\">\n" +
                                 "              <table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n" +
                                 "               <tbody>\n" +
                                 "                <tr>\n" +
                                 "                 <td width=\"58%\" class=\"reg_content\">Сортировать по: <span class=\"now_filter\">Наименованию</span>&nbsp;&nbsp;<a class=\"ashka\" href=\"/catalog/1141.html/brand/0/sort/1/page/1\">Цене</a></td>\n" +
                                 "                </tr>\n" +
                                 "               </tbody>\n" +
                                 "              </table></td>\n" +
                                 "            </tr>\n" +
                                 "            <tr>\n" +
                                 "             <td colspan=\"2\">\n" +
                                 "              <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                                 "               <tbody>\n" +
                                 "                <tr>\n" +
                                 "                 <td>\n" +
                                 "                  <table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n" +
                                 "                   <tbody>\n" +
                                 "                    <tr>\n" +
                                 "                     <td class=\"catalog_content_cell\" width=\"33%\">\n" +
                                 "                      <table width=\"250\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin-bottom: 15px;\" itemprop=\"itemListElement\" itemscope itemtype=\"http://schema.org/Product\">\n" +
                                 "                       <tbody>\n" +
                                 "                        <tr>\n" +
                                 "                         <td colspan=\"2\" class=\"item_img_cell\"><img onclick=\"document.location='/product/1124856.html';return false\" itemprop=\"image\" src=\"/img/product/200/1124856_1_200.jpg\" alt=\"Изображение товара Планшет Samsung Galaxy Tab A9 SM-X115 4/64 ГБ LTE графитовый (РСТ)\" title=\"Купить Планшет Samsung Galaxy Tab A9 SM-X115 4/64 ГБ LTE графитовый (РСТ)\" width=\"200\" style=\"border:none; decoration: none; \"></td>\n" +
                                 "                        </tr>\n" +
                                 "                        <tr>\n" +
                                 "                         <td height=\"42\" colspan=\"2\" class=\"catalog_item_label_cell\"><a href=\"/product/1124856.html\" title=\"Купить Планшет Samsung Galaxy Tab A9 SM-X115 4/64 ГБ LTE графитовый (РСТ)\" itemprop=\"name\">Планшет Samsung Galaxy Tab A9 SM-X115 4/64 ГБ LTE графитовый (РСТ)</a></td>\n" +
                                 "                        </tr>\n" +
                                 "                        <tr>\n" +
                                 "                         <td class=\"price_cell\" itemprop=\"offers\" itemscope itemtype=\"http://schema.org/Offer\"><span itemprop=\"price\">16100</span><span itemprop=\"priceCurrency\" content=\"RUB\"></span>р.</td>\n" +
                                 "                         <td class=\"item_full_info\" id=\"text1124856\" onclick=\"addtobasket_w_fancy(1124856)\"><span title=\"Купить Планшет Samsung Galaxy Tab A9 SM-X115 4/64 ГБ LTE графитовый (РСТ)\" alt=\"Купить Планшет Samsung Galaxy Tab A9 SM-X115 4/64 ГБ LTE графитовый (РСТ)\" id=\"buyimg1124856\" class=\"buybutton\">Купить</span></td>\n" +
                                 "                        </tr>\n" +
                                 "                       </tbody>\n" +
                                 "                      </table></td>\n" +
                                 "                     <td class=\"catalog_content_cell\" width=\"33%\">\n" +
                                 "                      <table width=\"250\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin-bottom: 15px;\" itemprop=\"itemListElement\" itemscope itemtype=\"http://schema.org/Product\">\n" +
                                 "                       <tbody>\n" +
                                 "                        <tr>\n" +
                                 "                         <td colspan=\"2\" class=\"item_img_cell\"><img onclick=\"document.location='/product/1124850.html';return false\" itemprop=\"image\" src=\"/img/product/200/1124850_1_200.jpg\" alt=\"Изображение товара Планшет Samsung Galaxy Tab A9 SM-X115 8/128 ГБ LTE серебристый (РСТ)\" title=\"Купить Планшет Samsung Galaxy Tab A9 SM-X115 8/128 ГБ LTE серебристый (РСТ)\" width=\"200\" style=\"border:none; decoration: none; \"></td>\n" +
                                 "                        </tr>\n" +
                                 "                        <tr>\n" +
                                 "                         <td height=\"42\" colspan=\"2\" class=\"catalog_item_label_cell\"><a href=\"/product/1124850.html\" title=\"Купить Планшет Samsung Galaxy Tab A9 SM-X115 8/128 ГБ LTE серебристый (РСТ)\" itemprop=\"name\">Планшет Samsung Galaxy Tab A9 SM-X115 8/128 ГБ LTE серебристый (РСТ)</a></td>\n" +
                                 "                        </tr>\n" +
                                 "                        <tr>\n" +
                                 "                         <td class=\"price_cell\" itemprop=\"offers\" itemscope itemtype=\"http://schema.org/Offer\"><span itemprop=\"price\">20200</span><span itemprop=\"priceCurrency\" content=\"RUB\"></span>р.</td>\n" +
                                 "                         <td class=\"item_full_info\" id=\"text1124850\" onclick=\"addtobasket_w_fancy(1124850)\"><span title=\"Купить Планшет Samsung Galaxy Tab A9 SM-X115 8/128 ГБ LTE серебристый (РСТ)\" alt=\"Купить Планшет Samsung Galaxy Tab A9 SM-X115 8/128 ГБ LTE серебристый (РСТ)\" id=\"buyimg1124850\" class=\"buybutton\">Купить</span></td>\n" +
                                 "                        </tr>\n" +
                                 "                       </tbody>\n" +
                                 "                      </table></td>\n" +
                                 "                     <td class=\"catalog_content_cell\" width=\"33%\">\n" +
                                 "                      <table width=\"250\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin-bottom: 15px;\" itemprop=\"itemListElement\" itemscope itemtype=\"http://schema.org/Product\">\n" +
                                 "                       <tbody>\n" +
                                 "                        <tr>\n" +
                                 "                         <td colspan=\"2\" class=\"item_img_cell\"><img onclick=\"document.location='/product/1125023.html';return false\" itemprop=\"image\" src=\"/img/product/200/1125023_1_200.jpg\" alt=\"Изображение товара Планшет Samsung Galaxy Tab A9 SM-X115 8/128 ГБ LTE темно-синий (Global Version)\" title=\"Купить Планшет Samsung Galaxy Tab A9 SM-X115 8/128 ГБ LTE темно-синий (Global Version)\" width=\"200\" style=\"border:none; decoration: none; \"></td>\n" +
                                 "                        </tr>\n" +
                                 "                        <tr>\n" +
                                 "                         <td height=\"42\" colspan=\"2\" class=\"catalog_item_label_cell\"><a href=\"/product/1125023.html\" title=\"Купить Планшет Samsung Galaxy Tab A9 SM-X115 8/128 ГБ LTE темно-синий (Global Version)\" itemprop=\"name\">Планшет Samsung Galaxy Tab A9 SM-X115 8/128 ГБ LTE темно-синий (Global Version)</a></td>\n" +
                                 "                        </tr>\n" +
                                 "                        <tr>\n" +
                                 "                         <td class=\"price_cell\" itemprop=\"offers\" itemscope itemtype=\"http://schema.org/Offer\"><span itemprop=\"price\">19750</span><span itemprop=\"priceCurrency\" content=\"RUB\"></span>р.</td>\n" +
                                 "                         <td class=\"item_full_info\" id=\"text1125023\" onclick=\"addtobasket_w_fancy(1125023)\"><span title=\"Купить Планшет Samsung Galaxy Tab A9 SM-X115 8/128 ГБ LTE темно-синий (Global Version)\" alt=\"Купить Планшет Samsung Galaxy Tab A9 SM-X115 8/128 ГБ LTE темно-синий (Global Version)\" id=\"buyimg1125023\" class=\"buybutton\">Купить</span></td>\n" +
                                 "                        </tr>\n" +
                                 "                       </tbody>\n" +
                                 "                      </table></td>\n" +
                                 "                    </tr>\n" +
                                 "                    <tr>\n" +
                                 "                     <td class=\"catalog_content_cell\" width=\"33%\">\n" +
                                 "                      <table width=\"250\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin-bottom: 15px;\" itemprop=\"itemListElement\" itemscope itemtype=\"http://schema.org/Product\">\n" +
                                 "                       <tbody>\n" +
                                 "                        <tr>\n" +
                                 "                         <td colspan=\"2\" class=\"item_img_cell\"><img onclick=\"document.location='/product/1124415.html';return false\" itemprop=\"image\" src=\"/img/product/200/1124415_1_200.jpg\" alt=\"Изображение товара Планшет Xiaomi Pad 6 6/128 ГБ голубой (РСТ)\" title=\"Купить Планшет Xiaomi Pad 6 6/128 ГБ голубой (РСТ)\" width=\"200\" style=\"border:none; decoration: none; \"></td>\n" +
                                 "                        </tr>\n" +
                                 "                        <tr>\n" +
                                 "                         <td height=\"42\" colspan=\"2\" class=\"catalog_item_label_cell\"><a href=\"/product/1124415.html\" title=\"Купить Планшет Xiaomi Pad 6 6/128 ГБ голубой (РСТ)\" itemprop=\"name\">Планшет Xiaomi Pad 6 6/128 ГБ голубой (РСТ)</a></td>\n" +
                                 "                        </tr>\n" +
                                 "                        <tr>\n" +
                                 "                         <td class=\"price_cell\" itemprop=\"offers\" itemscope itemtype=\"http://schema.org/Offer\"><span itemprop=\"price\">28800</span><span itemprop=\"priceCurrency\" content=\"RUB\"></span>р.</td>\n" +
                                 "                         <td class=\"item_full_info\" id=\"text1124415\" onclick=\"addtobasket_w_fancy(1124415)\"><span title=\"Купить Планшет Xiaomi Pad 6 6/128 ГБ голубой (РСТ)\" alt=\"Купить Планшет Xiaomi Pad 6 6/128 ГБ голубой (РСТ)\" id=\"buyimg1124415\" class=\"buybutton\">Купить</span></td>\n" +
                                 "                        </tr>\n" +
                                 "                       </tbody>\n" +
                                 "                      </table></td>\n" +
                                 "                     <td class=\"catalog_content_cell\" width=\"33%\">\n" +
                                 "                      <table width=\"250\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin-bottom: 15px;\" itemprop=\"itemListElement\" itemscope itemtype=\"http://schema.org/Product\">\n" +
                                 "                       <tbody>\n" +
                                 "                        <tr>\n" +
                                 "                         <td colspan=\"2\" class=\"item_img_cell\"><img onclick=\"document.location='/product/1124417.html';return false\" itemprop=\"image\" src=\"/img/product/200/1124417_1_200.jpg\" alt=\"Изображение товара Планшет Xiaomi Pad 6 6/128 ГБ серый (РСТ)\" title=\"Купить Планшет Xiaomi Pad 6 6/128 ГБ серый (РСТ)\" width=\"200\" style=\"border:none; decoration: none; \"></td>\n" +
                                 "                        </tr>\n" +
                                 "                        <tr>\n" +
                                 "                         <td height=\"42\" colspan=\"2\" class=\"catalog_item_label_cell\"><a href=\"/product/1124417.html\" title=\"Купить Планшет Xiaomi Pad 6 6/128 ГБ серый (РСТ)\" itemprop=\"name\">Планшет Xiaomi Pad 6 6/128 ГБ серый (РСТ)</a></td>\n" +
                                 "                        </tr>\n" +
                                 "                        <tr>\n" +
                                 "                         <td class=\"price_cell\" itemprop=\"offers\" itemscope itemtype=\"http://schema.org/Offer\"><span itemprop=\"price\">28000</span><span itemprop=\"priceCurrency\" content=\"RUB\"></span>р.</td>\n" +
                                 "                         <td class=\"item_full_info\" id=\"text1124417\" onclick=\"addtobasket_w_fancy(1124417)\"><span title=\"Купить Планшет Xiaomi Pad 6 6/128 ГБ серый (РСТ)\" alt=\"Купить Планшет Xiaomi Pad 6 6/128 ГБ серый (РСТ)\" id=\"buyimg1124417\" class=\"buybutton\">Купить</span></td>\n" +
                                 "                        </tr>\n" +
                                 "                       </tbody>\n" +
                                 "                      </table></td>\n" +
                                 "                     <td class=\"catalog_content_cell\" width=\"33%\">\n" +
                                 "                      <table width=\"250\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin-bottom: 15px;\" itemprop=\"itemListElement\" itemscope itemtype=\"http://schema.org/Product\">\n" +
                                 "                       <tbody>\n" +
                                 "                        <tr>\n" +
                                 "                         <td colspan=\"2\" class=\"item_img_cell\"><img onclick=\"document.location='/product/1124420.html';return false\" itemprop=\"image\" src=\"/img/product/200/1124420_1_200.jpg\" alt=\"Изображение товара Планшет Xiaomi Pad 6 8/256ГБ серый (РСТ)\" title=\"Купить Планшет Xiaomi Pad 6 8/256ГБ серый (РСТ)\" width=\"200\" style=\"border:none; decoration: none; \"></td>\n" +
                                 "                        </tr>\n" +
                                 "                        <tr>\n" +
                                 "                         <td height=\"42\" colspan=\"2\" class=\"catalog_item_label_cell\"><a href=\"/product/1124420.html\" title=\"Купить Планшет Xiaomi Pad 6 8/256ГБ серый (РСТ)\" itemprop=\"name\">Планшет Xiaomi Pad 6 8/256ГБ серый (РСТ)</a></td>\n" +
                                 "                        </tr>\n" +
                                 "                        <tr>\n" +
                                 "                         <td class=\"price_cell\" itemprop=\"offers\" itemscope itemtype=\"http://schema.org/Offer\"><span itemprop=\"price\">29850</span><span itemprop=\"priceCurrency\" content=\"RUB\"></span>р.</td>\n" +
                                 "                         <td class=\"item_full_info\" id=\"text1124420\" onclick=\"addtobasket_w_fancy(1124420)\"><span title=\"Купить Планшет Xiaomi Pad 6 8/256ГБ серый (РСТ)\" alt=\"Купить Планшет Xiaomi Pad 6 8/256ГБ серый (РСТ)\" id=\"buyimg1124420\" class=\"buybutton\">Купить</span></td>\n" +
                                 "                        </tr>\n" +
                                 "                       </tbody>\n" +
                                 "                      </table></td>\n" +
                                 "                    </tr>\n" +
                                 "                    <tr>\n" +
                                 "                     <td class=\"catalog_content_cell\" width=\"33%\">\n" +
                                 "                      <table width=\"250\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin-bottom: 15px;\" itemprop=\"itemListElement\" itemscope itemtype=\"http://schema.org/Product\">\n" +
                                 "                       <tbody>\n" +
                                 "                        <tr>\n" +
                                 "                         <td colspan=\"2\" class=\"item_img_cell\"><img onclick=\"document.location='/product/1125302.html';return false\" itemprop=\"image\" src=\"/img/product/200/1125302_1_200.jpg\" alt=\"Изображение товара Планшет Xiaomi Redmi Pad SE 8/256 ГБ graphite gray (Global Version)\" title=\"Купить Планшет Xiaomi Redmi Pad SE 8/256 ГБ graphite gray (Global Version)\" width=\"200\" style=\"border:none; decoration: none; \"></td>\n" +
                                 "                        </tr>\n" +
                                 "                        <tr>\n" +
                                 "                         <td height=\"42\" colspan=\"2\" class=\"catalog_item_label_cell\"><a href=\"/product/1125302.html\" title=\"Купить Планшет Xiaomi Redmi Pad SE 8/256 ГБ graphite gray (Global Version)\" itemprop=\"name\">Планшет Xiaomi Redmi Pad SE 8/256 ГБ graphite gray (Global Version)</a></td>\n" +
                                 "                        </tr>\n" +
                                 "                        <tr>\n" +
                                 "                         <td class=\"price_cell\" itemprop=\"offers\" itemscope itemtype=\"http://schema.org/Offer\"><span itemprop=\"price\">17250</span><span itemprop=\"priceCurrency\" content=\"RUB\"></span>р.</td>\n" +
                                 "                         <td class=\"item_full_info\" id=\"text1125302\" onclick=\"addtobasket_w_fancy(1125302)\"><span title=\"Купить Планшет Xiaomi Redmi Pad SE 8/256 ГБ graphite gray (Global Version)\" alt=\"Купить Планшет Xiaomi Redmi Pad SE 8/256 ГБ graphite gray (Global Version)\" id=\"buyimg1125302\" class=\"buybutton\">Купить</span></td>\n" +
                                 "                        </tr>\n" +
                                 "                       </tbody>\n" +
                                 "                      </table></td>\n" +
                                 "                     <td class=\"catalog_content_cell\" width=\"33%\">\n" +
                                 "                      <table width=\"250\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin-bottom: 15px;\" itemprop=\"itemListElement\" itemscope itemtype=\"http://schema.org/Product\">\n" +
                                 "                       <tbody>\n" +
                                 "                        <tr>\n" +
                                 "                         <td colspan=\"2\" class=\"item_img_cell\"><img onclick=\"document.location='/product/1125273.html';return false\" itemprop=\"image\" src=\"/img/product/200/1125273_1_200.jpg\" alt=\"Изображение товара Планшет Xiaomi Redmi Pad SE 8/256 ГБ lavender purple (РСТ)\" title=\"Купить Планшет Xiaomi Redmi Pad SE 8/256 ГБ lavender purple (РСТ)\" width=\"200\" style=\"border:none; decoration: none; \"></td>\n" +
                                 "                        </tr>\n" +
                                 "                        <tr>\n" +
                                 "                         <td height=\"42\" colspan=\"2\" class=\"catalog_item_label_cell\"><a href=\"/product/1125273.html\" title=\"Купить Планшет Xiaomi Redmi Pad SE 8/256 ГБ lavender purple (РСТ)\" itemprop=\"name\">Планшет Xiaomi Redmi Pad SE 8/256 ГБ lavender purple (РСТ)</a></td>\n" +
                                 "                        </tr>\n" +
                                 "                        <tr>\n" +
                                 "                         <td class=\"price_cell\" itemprop=\"offers\" itemscope itemtype=\"http://schema.org/Offer\"><span itemprop=\"price\">17980</span><span itemprop=\"priceCurrency\" content=\"RUB\"></span>р.</td>\n" +
                                 "                         <td class=\"item_full_info\" id=\"text1125273\" onclick=\"addtobasket_w_fancy(1125273)\"><span title=\"Купить Планшет Xiaomi Redmi Pad SE 8/256 ГБ lavender purple (РСТ)\" alt=\"Купить Планшет Xiaomi Redmi Pad SE 8/256 ГБ lavender purple (РСТ)\" id=\"buyimg1125273\" class=\"buybutton\">Купить</span></td>\n" +
                                 "                        </tr>\n" +
                                 "                       </tbody>\n" +
                                 "                      </table></td>\n" +
                                 "                     <td class=\"catalog_content_cell\" width=\"33%\">\n" +
                                 "                      <table width=\"250\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin-bottom: 15px;\" itemprop=\"itemListElement\" itemscope itemtype=\"http://schema.org/Product\">\n" +
                                 "                       <tbody>\n" +
                                 "                        <tr>\n" +
                                 "                         <td colspan=\"2\" class=\"item_img_cell\"><img onclick=\"document.location='/product/1125281.html';return false\" itemprop=\"image\" src=\"/img/product/200/1125281_1_200.jpg\" alt=\"Изображение товара Планшет Xiaomi Redmi Pad SE 8/256 ГБ mint green (Global Version)\" title=\"Купить Планшет Xiaomi Redmi Pad SE 8/256 ГБ mint green (Global Version)\" width=\"200\" style=\"border:none; decoration: none; \"></td>\n" +
                                 "                        </tr>\n" +
                                 "                        <tr>\n" +
                                 "                         <td height=\"42\" colspan=\"2\" class=\"catalog_item_label_cell\"><a href=\"/product/1125281.html\" title=\"Купить Планшет Xiaomi Redmi Pad SE 8/256 ГБ mint green (Global Version)\" itemprop=\"name\">Планшет Xiaomi Redmi Pad SE 8/256 ГБ mint green (Global Version)</a></td>\n" +
                                 "                        </tr>\n" +
                                 "                        <tr>\n" +
                                 "                         <td class=\"price_cell\" itemprop=\"offers\" itemscope itemtype=\"http://schema.org/Offer\"><span itemprop=\"price\">17250</span><span itemprop=\"priceCurrency\" content=\"RUB\"></span>р.</td>\n" +
                                 "                         <td class=\"item_full_info\" id=\"text1125281\" onclick=\"addtobasket_w_fancy(1125281)\"><span title=\"Купить Планшет Xiaomi Redmi Pad SE 8/256 ГБ mint green (Global Version)\" alt=\"Купить Планшет Xiaomi Redmi Pad SE 8/256 ГБ mint green (Global Version)\" id=\"buyimg1125281\" class=\"buybutton\">Купить</span></td>\n" +
                                 "                        </tr>\n" +
                                 "                       </tbody>\n" +
                                 "                      </table></td>\n" +
                                 "                    </tr>\n" +
                                 "                    <tr>\n" +
                                 "                     <td colspan=\"3\" class=\"pager_cell2\"><span class=\"now_page_number\">&nbsp;1&nbsp;</span></td>\n" +
                                 "                    </tr>\n" +
                                 "                    <tr>\n" +
                                 "                     <td colspan=\"3\" class=\"reg_content\"></td>\n" +
                                 "                    </tr>\n" +
                                 "                   </tbody>\n" +
                                 "                  </table></td>\n" +
                                 "                </tr>\n" +
                                 "               </tbody>\n" +
                                 "              </table></td>\n" +
                                 "            </tr>\n" +
                                 "           </tbody>\n" +
                                 "          </table></td>\n" +
                                 "        </tr>\n" +
                                 "        <tr>\n" +
                                 "         <td colspan=\"3\" align=\"center\">\n" +
                                 "          <div class=\"footer\">\n" +
                                 "           <div class=\"footer_block\">\n" +
                                 "            <span class=\"footer_h1\">Информация</span>\n" +
                                 "            <br><a href=\"/\">Наши спецпредложения</a>\n" +
                                 "            <br><a href=\"/dostavka.html\">Доставка</a>\n" +
                                 "            <br><a href=\"/payment.html\">Оплата</a>\n" +
                                 "            <br><a href=\"/warranty.html\">Гарантия</a>\n" +
                                 "            <br><a href=\"/contacts.html\">Контакты</a>\n" +
                                 "            <br><a href=\"/privacy_policy.html\">Положение о конфиденциальности и защите персональных данных</a>\n" +
                                 "           </div>\n" +
                                 "           <div class=\"footer_block_cont\">\n" +
                                 "            <span class=\"footer_tel\">+7(495)143-77-71</span>\n" +
                                 "            <br>\n" +
                                 "            <br><a class=\"footer_email\" href=\"http://vk.com/playback_ru\" target=\"_blank\"><img src=\"/img/VK.png\" title=\"Наша страница Вконтакте\"></a> &nbsp;&nbsp; \n" +
                                 "            <br>\n" +
                                 "            <br>\n" +
                                 "           </div>\n" +
                                 "           <div class=\"footer_block_cont\" style=\"width:260px;\">\n" +
                                 "            <span class=\"footer_h1\">График работы:</span>\n" +
                                 "            <br>\n" +
                                 "             пн-пт: c 11-00 до 20-00 \n" +
                                 "            <br>\n" +
                                 "             сб-вс: с 11-00 до 18-00 \n" +
                                 "            <br>\n" +
                                 "            <br><span class=\"footer_h1\">Наш адрес:</span>\n" +
                                 "            <br>\n" +
                                 "             Москва, Звездный бульвар, 10, \n" +
                                 "            <br>\n" +
                                 "             строение 1, 2 этаж, офис 10.\n" +
                                 "           </div>\n" +
                                 "           <div class=\"footer_block\">\n" +
                                 "           </div>\n" +
                                 "           <div class=\"footer_block\">\n" +
                                 "            <script type=\"text/javascript\" src=\"//vk.com/js/api/openapi.js?105\"></script>\n" +
                                 "            <div id=\"vk_groups\"></div>\n" +
                                 "            <script type=\"text/javascript\">\n" +
                                 "VK.Widgets.Group(\"vk_groups\", {mode: 0, width: \"260\", height: \"210\", color1: 'FFFFFF', color2: '0C5696', color3: '0064BA'}, 48023501);\n" +
                                 "</script>\n" +
                                 "           </div>\n" +
                                 "          </div>\n" +
                                 "          <div style=\"width: 1024px; font-family: Roboto-Regular,Helvetica,sans-serif; text-align: right; font-size: 12px; text-align: left; padding-left: 10px; color: #595959; background: url(/img/footer-fon.png) repeat;\">\n" +
                                 "           2005-2024 ©Интернет магазин PlayBack.ru\n" +
                                 "          </div><!-- Yandex.Metrika counter -->\n" +
                                 "          <script type=\"text/javascript\">\n" +
                                 "   (function(m,e,t,r,i,k,a){m[i]=m[i]||function(){(m[i].a=m[i].a||[]).push(arguments)};\n" +
                                 "   m[i].l=1*new Date();k=e.createElement(t),a=e.getElementsByTagName(t)[0],k.async=1,k.src=r,a.parentNode.insertBefore(k,a)})\n" +
                                 "   (window, document, \"script\", \"https://mc.yandex.ru/metrika/tag.js\", \"ym\");\n" +
                                 "\n" +
                                 "   ym(232370, \"init\", {\n" +
                                 "        clickmap:true,\n" +
                                 "        trackLinks:true,\n" +
                                 "        accurateTrackBounce:true,\n" +
                                 "        webvisor:true\n" +
                                 "   });\n" +
                                 "</script>\n" +
                                 "          <noscript>\n" +
                                 "           <div>\n" +
                                 "            <img src=\"https://mc.yandex.ru/watch/232370\" style=\"position:absolute; left:-9999px;\" alt=\"\">\n" +
                                 "           </div>\n" +
                                 "          </noscript><!-- /Yandex.Metrika counter --> <!-- BEGIN JIVOSITE CODE {literal} -->\n" +
                                 "          <script type=\"text/javascript\">\n" +
                                 "(function(){ var widget_id = '8LKJc6dMce';var d=document;var w=window;function l(){\n" +
                                 "  var s = document.createElement('script'); s.type = 'text/javascript'; s.async = true;\n" +
                                 "  s.src = '//code.jivosite.com/script/widget/'+widget_id\n" +
                                 "    ; var ss = document.getElementsByTagName('script')[0]; ss.parentNode.insertBefore(s, ss);}\n" +
                                 "  if(d.readyState=='complete'){l();}else{if(w.attachEvent){w.attachEvent('onload',l);}\n" +
                                 "  else{w.addEventListener('load',l,false);}}})();\n" +
                                 "</script><!-- {/literal} END JIVOSITE CODE --></td>\n" +
                                 "        </tr>\n" +
                                 "       </tbody>\n" +
                                 "      </table><a href=\"#\" class=\"scrollup\">Наверх</a></td>\n" +
                                 "    </tr>\n" +
                                 "   </tbody>\n" +
                                 "  </table>\n" +
                                 " </body>\n" +
                                 "</html>";

    public static void main(String[] args) throws IOException {
        LemmaFinder instance = LemmaFinder.getInstance();
        Map<String, Integer> map = instance.collectLemmas(text);

        // Распечатываем содержимое HashMap
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            System.out.println(entry.getKey() + " - " + entry.getValue());
        }

        Set<String> set = instance.getLemmaSet(text);

        // Распечатываем содержимое HashSet
        for (String value : set) {
            System.out.println(value);
        }
    }
}
