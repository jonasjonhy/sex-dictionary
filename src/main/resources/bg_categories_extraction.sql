select cate_name as category
from mbproduct.product_lib_category 
where status = 1
and (length(cate_name) = 4 or length(cate_name) = 6 or length(cate_name) = 9)
and cate_name != '' and cate_name != '其他' and cate_name != '其它'
group by cate_name
order by category_id;