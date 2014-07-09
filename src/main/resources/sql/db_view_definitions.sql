
-- standard_variable_summary view : Standard variable summary information modelled as per http://gmod.org/wiki/Chado_Tables#Table:_cvterm
-- Definitin of this view is availabe at https://github.com/digitalabs/IBDBScripts/blob/master/local/common/10_IBDBV2-DMS-Views_20140707.sql
		
-- view for germplasm search
		
drop view if exists germplasm_summary;

	create view germplasm_summary as
	
		select gp.gid, group_concat(n.nval) as names, ld.listid
		
		from germplsm gp 
			
			inner join names n on n.gid = gp.gid
			left outer join listdata ld on ld.gid = n.gid
		
		where gp.gid != gp.grplce and n.nstat != 9
		group by gid
	;
	

drop view if exists germplasm_trial_details;
create view `germplasm_trial_details` AS
    select 
        `pr`.`object_project_id` AS `study_id`,
        `ep`.`project_id` AS `project_id`,
        `e`.`type_id` AS `type_id`,
        `e`.`nd_geolocation_id` AS `envt_id`,
        `e`.`type_id` AS `observation_type`,
        `e`.`nd_experiment_id` AS `experiment_id`,
        `p`.`phenotype_id` AS `phenotype_id`,
        `td`.`trait_name` AS `trait_name`,
        `svd`.`cvterm_id` AS `stdvar_id`,
        `svd`.`stdvar_name` AS `stdvar_name`,
        `p`.`value` AS `observed_value`,
        `s`.`stock_id` AS `stock_id`,
        `s`.`name` AS `entry_designation`,
        `g`.`gid` AS `gid`
    from
        (((((((((`stock` `s`
        join `nd_experiment_stock` `es` ON ((`es`.`stock_id` = `s`.`stock_id`)))
        join `nd_experiment` `e` ON ((`e`.`nd_experiment_id` = `es`.`nd_experiment_id`)))
        join `nd_experiment_project` `ep` ON ((`ep`.`nd_experiment_id` = `e`.`nd_experiment_id`)))
        join `nd_experiment_phenotype` `epx` ON ((`epx`.`nd_experiment_id` = `e`.`nd_experiment_id`)))
        join `phenotype` `p` ON ((`p`.`phenotype_id` = `epx`.`phenotype_id`)))
        join `standard_variable_details` `svd` ON ((`svd`.`cvterm_id` = `p`.`observable_id`)))
        join `trait_details` `td` ON ((`td`.`trait_id` = `svd`.`property_id`)))
        join `project_relationship` `pr` ON ((`pr`.`subject_project_id` = `ep`.`project_id`)))
        join `germplsm` `g` ON ((`s`.`dbxref_id` = `g`.`gid`)))
    where
        ((`e`.`type_id` = 1170)
            or ((`e`.`type_id` = 1155)
            and (1 = (select 
                count(0)
            from
                `project_relationship`
            where
                ((`project_relationship`.`object_project_id` = `pr`.`object_project_id`)
                    and (`project_relationship`.`type_id` = 1150))))))
    order by `ep`.`project_id` , `e`.`nd_geolocation_id` , `e`.`type_id` , `td`.`trait_name` , `s`.`name`;