select
	channel_goods.id as id,
	channel_goods.product_sys_code as productCode,
	channel_goods.product_name as productName,
	brand.brand_name as brandName,
	category.cate_name as categoryName
from
	product_channel_goods as channel_goods
inner join
	product_seller_goods as seller_goods
on
	channel_goods.product_sys_code = seller_goods.product_sys_code
left join
	product_lib_brand as brand
on
	seller_goods.brand_id = brand.brand_id
left join
	product_lib_category as category
on
	seller_goods.category_id = category.category_id
where
	channel_goods.status = 1
	and seller_goods.status = 1
	and channel_goods.channel_code = 'HQ01S116'
order by 
	channel_goods.id
limit ?, 1000;